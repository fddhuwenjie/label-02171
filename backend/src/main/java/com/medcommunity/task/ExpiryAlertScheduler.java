package com.medcommunity.task;

import com.medcommunity.service.DrugExpiryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 药品效期预警定时任务。
 * 每天凌晨2点扫描所有库存，对距离过期不足90天的药品生成预警记录。
 */
@Slf4j
@Component
public class ExpiryAlertScheduler {

    @Autowired
    private DrugExpiryAlertService drugExpiryAlertService;

    /**
     * 每天凌晨02:00执行效期预警扫描。
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyExpiryScan() {
        log.info("[定时任务] 开始每日药品效期预警扫描");
        try {
            drugExpiryAlertService.scanAndGenerateAlerts();
            log.info("[定时任务] 每日药品效期预警扫描完成");
        } catch (Exception e) {
            log.error("[定时任务] 药品效期预警扫描异常", e);
        }
    }
}
