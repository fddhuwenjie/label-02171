package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.DrugExpiryAlert;
import com.medcommunity.entity.DrugInventory;
import com.medcommunity.exception.BusinessException;
import com.medcommunity.mapper.DrugExpiryAlertMapper;
import com.medcommunity.mapper.DrugInventoryMapper;
import com.medcommunity.service.DrugExpiryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class DrugExpiryAlertServiceImpl implements DrugExpiryAlertService {

    @Autowired
    private DrugExpiryAlertMapper drugExpiryAlertMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    private static final int WARNING_DAYS = 90;
    private static final int URGENT_DAYS = 30;

    @Override
    public IPage<DrugExpiryAlert> pageList(Long drugId, Long hospitalId, String alertLevel, String status, int page, int size) {
        Page<DrugExpiryAlert> pageParam = new Page<>(page, size);
        return drugExpiryAlertMapper.selectAlertList(pageParam, drugId, hospitalId, alertLevel, status);
    }

    @Override
    public List<DrugExpiryAlert> getActiveAlerts() {
        return drugExpiryAlertMapper.selectActiveAlerts();
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void scanAndGenerateAlerts() {
        log.info("开始执行药品效期预警扫描任务");
        LocalDate today = LocalDate.now();

        List<DrugInventory> inventoryList = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>()
                        .isNotNull(DrugInventory::getExpireDate)
                        .gt(DrugInventory::getQuantity, 0)
        );

        int newAlertCount = 0;
        int updateAlertCount = 0;
        int resolveAlertCount = 0;

        for (DrugInventory inventory : inventoryList) {
            LocalDate expireDate = inventory.getExpireDate();
            long daysLeft = ChronoUnit.DAYS.between(today, expireDate);

            DrugExpiryAlert existingAlert = drugExpiryAlertMapper.selectActiveByInventoryId(inventory.getId());

            if (daysLeft <= WARNING_DAYS && daysLeft > 0) {
                String alertLevel = daysLeft <= URGENT_DAYS ? "URGENT" : "WARNING";

                if (existingAlert == null) {
                    DrugExpiryAlert alert = new DrugExpiryAlert();
                    alert.setInventoryId(inventory.getId());
                    alert.setDrugId(inventory.getDrugId());
                    alert.setHospitalId(inventory.getHospitalId());
                    alert.setBatchNo(inventory.getBatchNo());
                    alert.setExpireDate(expireDate);
                    alert.setDaysLeft((int) daysLeft);
                    alert.setQuantity(inventory.getQuantity());
                    alert.setAlertLevel(alertLevel);
                    alert.setStatus("ACTIVE");
                    drugExpiryAlertMapper.insert(alert);
                    newAlertCount++;
                } else {
                    existingAlert.setDaysLeft((int) daysLeft);
                    existingAlert.setAlertLevel(alertLevel);
                    existingAlert.setQuantity(inventory.getQuantity());
                    drugExpiryAlertMapper.updateById(existingAlert);
                    updateAlertCount++;
                }
            } else {
                if (existingAlert != null) {
                    existingAlert.setStatus("RESOLVED");
                    drugExpiryAlertMapper.updateById(existingAlert);
                    resolveAlertCount++;
                }
            }
        }

        log.info("药品效期预警扫描任务完成，新增：{}条，更新：{}条，解决：{}条",
                newAlertCount, updateAlertCount, resolveAlertCount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveAlert(Long id) {
        DrugExpiryAlert alert = drugExpiryAlertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException("预警记录不存在");
        }
        alert.setStatus("RESOLVED");
        drugExpiryAlertMapper.updateById(alert);
        log.info("标记预警为已解决，预警ID：{}", id);
    }

    @Override
    public boolean isNearExpiry(Long drugId, Long hospitalId, String batchNo) {
        DrugInventory inventory = drugInventoryMapper.selectOne(
                new LambdaQueryWrapper<DrugInventory>()
                        .eq(DrugInventory::getDrugId, drugId)
                        .eq(DrugInventory::getHospitalId, hospitalId)
                        .eq(DrugInventory::getBatchNo, batchNo)
        );
        if (inventory == null || inventory.getExpireDate() == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        long daysLeft = ChronoUnit.DAYS.between(today, inventory.getExpireDate());
        return daysLeft <= URGENT_DAYS;
    }
}
