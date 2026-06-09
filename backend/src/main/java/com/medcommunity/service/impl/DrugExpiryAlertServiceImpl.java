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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DrugExpiryAlertServiceImpl implements DrugExpiryAlertService {

    private static final int EXPIRY_WARNING_DAYS = 90;
    private static final int ALERT_LEVEL_RED_DAYS = 30;

    @Autowired
    private DrugExpiryAlertMapper drugExpiryAlertMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    /**
     * 分页查询效期预警
     */
    @Override
    public IPage<DrugExpiryAlert> pageList(Long hospitalId, String alertLevel, String status, int page, int size) {
        Page<DrugExpiryAlert> pageParam = new Page<>(page, size);
        return drugExpiryAlertMapper.selectAlertPage(pageParam, hospitalId, alertLevel, status);
    }

    /**
     * 获取活跃预警列表
     */
    @Override
    public List<DrugExpiryAlert> getActiveAlerts(Integer limit) {
        return drugExpiryAlertMapper.selectActiveAlerts(limit);
    }

    /**
     * 统计未处理预警数量
     */
    @Override
    public long countActiveAlerts() {
        return drugExpiryAlertMapper.countActiveAlerts();
    }

    /**
     * 处理预警，将状态设置为RESOLVED
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolve(Long id) {
        DrugExpiryAlert alert = drugExpiryAlertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException("预警记录不存在");
        }
        alert.setStatus("RESOLVED");
        alert.setUpdatedAt(LocalDateTime.now());
        drugExpiryAlertMapper.updateById(alert);
        log.info("处理效期预警: id={}", id);
    }

    /**
     * 每天定时扫描库存，对90天内过期的药品生成/更新预警记录。
     * <p>
     * 预警级别：
     * - RED（红色）：30天内过期
     * - YELLOW（黄色）：31~90天内过期
     * - EXPIRED（已过期）：已超过效期
     * <p>
     * 逻辑：
     * 1. 加载所有库存记录，筛选90天内过期或已过期的批次
     * 2. 加载所有现有ACTIVE预警
     * 3. 对已存在的ACTIVE预警：若批次不再满足预警条件（过期>90天）标记RESOLVED，否则更新days/quantity/level
     * 4. 对新发现的近效期批次，插入新预警
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void scanAndGenerateAlerts() {
        log.info("开始执行药品效期预警扫描...");
        LocalDate today = LocalDate.now();

        List<DrugInventory> allInventories = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>()
                        .isNotNull(DrugInventory::getExpireDate)
                        .gt(DrugInventory::getQuantity, 0)
        );

        List<DrugExpiryAlert> existingAlerts = drugExpiryAlertMapper.selectAllActiveAlerts();
        Map<Long, DrugExpiryAlert> existingByInventoryId = new HashMap<>();
        for (DrugExpiryAlert a : existingAlerts) {
            existingByInventoryId.put(a.getInventoryId(), a);
        }

        List<Long> currentInventoryIds = new ArrayList<>();
        int createdCount = 0;
        int updatedCount = 0;
        int resolvedCount = 0;

        for (DrugInventory inv : allInventories) {
            if (inv.getExpireDate() == null) continue;
            currentInventoryIds.add(inv.getId());
            long days = ChronoUnit.DAYS.between(today, inv.getExpireDate());

            boolean shouldAlert = days <= EXPIRY_WARNING_DAYS;
            DrugExpiryAlert existing = existingByInventoryId.get(inv.getId());

            if (shouldAlert) {
                String level = resolveLevel(days);
                if (existing != null) {
                    existing.setDaysToExpire((int) days);
                    existing.setQuantity(inv.getQuantity());
                    existing.setAlertLevel(level);
                    existing.setUpdatedAt(LocalDateTime.now());
                    drugExpiryAlertMapper.updateById(existing);
                    updatedCount++;
                } else {
                    DrugExpiryAlert alert = new DrugExpiryAlert();
                    alert.setInventoryId(inv.getId());
                    alert.setDrugId(inv.getDrugId());
                    alert.setHospitalId(inv.getHospitalId());
                    alert.setBatchNo(inv.getBatchNo());
                    alert.setExpireDate(inv.getExpireDate());
                    alert.setDaysToExpire((int) days);
                    alert.setQuantity(inv.getQuantity());
                    alert.setAlertLevel(level);
                    alert.setStatus("ACTIVE");
                    alert.setCreatedAt(LocalDateTime.now());
                    alert.setUpdatedAt(LocalDateTime.now());
                    drugExpiryAlertMapper.insert(alert);
                    createdCount++;
                }
            } else {
                if (existing != null) {
                    existing.setStatus("RESOLVED");
                    existing.setUpdatedAt(LocalDateTime.now());
                    drugExpiryAlertMapper.updateById(existing);
                    resolvedCount++;
                }
            }
        }

        for (DrugExpiryAlert a : existingAlerts) {
            if (!currentInventoryIds.contains(a.getInventoryId())) {
                a.setStatus("RESOLVED");
                a.setUpdatedAt(LocalDateTime.now());
                drugExpiryAlertMapper.updateById(a);
                resolvedCount++;
            }
        }

        log.info("药品效期预警扫描完成：新建={}, 更新={}, 解除={}", createdCount, updatedCount, resolvedCount);
    }

    private String resolveLevel(long days) {
        if (days < 0) {
            return "EXPIRED";
        }
        if (days <= ALERT_LEVEL_RED_DAYS) {
            return "RED";
        }
        return "YELLOW";
    }
}
