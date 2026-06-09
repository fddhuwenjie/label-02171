package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.DrugExpiryAlert;
import com.medcommunity.entity.DrugInventory;
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

/**
 * 药品效期预警 Service 实现。
 */
@Slf4j
@Service
public class DrugExpiryAlertServiceImpl implements DrugExpiryAlertService {

    /** 预警阈值：距离过期不足该天数视为预警。 */
    private static final long EXPIRY_WARNING_DAYS = 90L;

    /** 高危阈值：距离过期不足该天数视为 CRITICAL。 */
    private static final long EXPIRY_CRITICAL_DAYS = 30L;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    @Autowired
    private DrugExpiryAlertMapper drugExpiryAlertMapper;

    /**
     * 扫描全部库存：
     * - days &lt; 0   : EXPIRED
     * - days &lt; 30  : CRITICAL
     * - days &lt; 90  : WARNING
     * 同一 inventory_id 的预警按 inventory_id 唯一约束做幂等更新。
     *
     * @return 本次写入/更新的条数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int scanAndGenerateAlerts() {
        LocalDate today = LocalDate.now();
        // 拉取所有有 expire_date 的库存
        List<DrugInventory> list = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>().isNotNull(DrugInventory::getExpireDate)
        );
        int count = 0;
        for (DrugInventory inv : list) {
            if (inv.getExpireDate() == null) {
                continue;
            }
            long days = ChronoUnit.DAYS.between(today, inv.getExpireDate());
            if (days >= EXPIRY_WARNING_DAYS) {
                continue;
            }
            String level = resolveAlertLevel(days);
            DrugExpiryAlert exists = drugExpiryAlertMapper.selectOne(
                    new LambdaQueryWrapper<DrugExpiryAlert>()
                            .eq(DrugExpiryAlert::getInventoryId, inv.getId())
                            .last("LIMIT 1")
            );
            if (exists == null) {
                DrugExpiryAlert alert = new DrugExpiryAlert();
                alert.setInventoryId(inv.getId());
                alert.setDrugId(inv.getDrugId());
                alert.setHospitalId(inv.getHospitalId());
                alert.setBatchNo(inv.getBatchNo());
                alert.setExpireDate(inv.getExpireDate());
                alert.setDaysToExpire((int) days);
                alert.setAlertLevel(level);
                alert.setStatus(1);
                drugExpiryAlertMapper.insert(alert);
                count++;
            } else {
                exists.setDaysToExpire((int) days);
                exists.setAlertLevel(level);
                exists.setExpireDate(inv.getExpireDate());
                exists.setBatchNo(inv.getBatchNo());
                drugExpiryAlertMapper.updateById(exists);
                count++;
            }
        }
        log.info("效期预警扫描完成，共处理 {} 条记录", count);
        return count;
    }

    /**
     * 根据剩余天数判定预警等级。
     *
     * @param days 剩余天数（可为负数）
     * @return 预警等级
     */
    private String resolveAlertLevel(long days) {
        if (days < 0) {
            return "EXPIRED";
        }
        if (days < EXPIRY_CRITICAL_DAYS) {
            return "CRITICAL";
        }
        return "WARNING";
    }

    @Override
    public IPage<DrugExpiryAlert> pageList(Long drugId, Long hospitalId, String alertLevel, Integer status, int page, int size) {
        Page<DrugExpiryAlert> pageParam = new Page<>(page, size);
        return drugExpiryAlertMapper.selectAlertPage(pageParam, drugId, hospitalId, alertLevel, status);
    }

    @Override
    public List<DrugExpiryAlert> getRecentAlerts(int limit) {
        return drugExpiryAlertMapper.selectRecentAlerts(limit > 0 ? limit : 10);
    }
}
