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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class DrugExpiryAlertServiceImpl implements DrugExpiryAlertService {

    private static final int EXPIRY_WARNING_DAYS = 90;

    @Autowired
    private DrugExpiryAlertMapper drugExpiryAlertMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public IPage<DrugExpiryAlert> pageList(String status, int page, int size) {
        Page<DrugExpiryAlert> pageParam = new Page<>(page, size);
        return drugExpiryAlertMapper.selectAlertList(pageParam, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DrugExpiryAlert> getActiveAlerts() {
        return drugExpiryAlertMapper.selectActiveAlerts();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scanAndGenerateAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate warningDate = today.plusDays(EXPIRY_WARNING_DAYS);

        List<DrugInventory> allInventory = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>()
                        .isNotNull(DrugInventory::getExpireDate)
                        .le(DrugInventory::getExpireDate, warningDate)
                        .gt(DrugInventory::getQuantity, 0)
        );

        int newAlertCount = 0;
        for (DrugInventory inventory : allInventory) {
            DrugExpiryAlert existingAlert = drugExpiryAlertMapper.selectActiveByInventoryId(inventory.getId());
            if (existingAlert != null) {
                existingAlert.setDaysUntilExpiry((int) ChronoUnit.DAYS.between(today, inventory.getExpireDate()));
                drugExpiryAlertMapper.updateById(existingAlert);
                continue;
            }

            long daysUntilExpiry = ChronoUnit.DAYS.between(today, inventory.getExpireDate());
            DrugExpiryAlert alert = new DrugExpiryAlert();
            alert.setDrugId(inventory.getDrugId());
            alert.setHospitalId(inventory.getHospitalId());
            alert.setInventoryId(inventory.getId());
            alert.setBatchNo(inventory.getBatchNo());
            alert.setExpireDate(inventory.getExpireDate());
            alert.setDaysUntilExpiry((int) daysUntilExpiry);
            alert.setStatus("ACTIVE");
            drugExpiryAlertMapper.insert(alert);
            newAlertCount++;
        }

        log.info("效期预警扫描完成: 扫描库存记录{}条, 新增预警{}条", allInventory.size(), newAlertCount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long id) {
        DrugExpiryAlert alert = drugExpiryAlertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException("预警记录不存在");
        }
        alert.setStatus("RESOLVED");
        drugExpiryAlertMapper.updateById(alert);
        log.info("效期预警已处理: alertId={}", id);
    }
}
