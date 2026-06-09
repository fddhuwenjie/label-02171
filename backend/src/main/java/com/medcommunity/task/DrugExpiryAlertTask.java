package com.medcommunity.task;

import com.medcommunity.service.DrugExpiryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 药品效期预警定时任务。
 *
 * <p>每天凌晨 02:00 扫描所有库存记录，对距离过期日期不足 90 天的药品生成 / 更新预警记录。</p>
 */
@Slf4j
@Component
public class DrugExpiryAlertTask {

    @Autowired
    private DrugExpiryAlertService drugExpiryAlertService;

    /**
     * 每日 02:00 触发扫描。
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scanDaily() {
        log.info("[DrugExpiryAlertTask] 开始每日效期预警扫描");
        try {
            int count = drugExpiryAlertService.scanAndGenerateAlerts();
            log.info("[DrugExpiryAlertTask] 扫描完成，处理 {} 条预警", count);
        } catch (Exception e) {
            log.error("[DrugExpiryAlertTask] 扫描异常", e);
        }
    }
}
