package com.medcommunity.schedule;

import com.medcommunity.service.DrugExpiryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 药品效期预警定时任务。
 * 每天凌晨1点扫描所有库存记录，对距离过期日期不足90天的药品生成预警记录。
 */
@Slf4j
@Component
public class ExpiryAlertSchedule {

    @Autowired
    private DrugExpiryAlertService drugExpiryAlertService;

    /**
     * 每天凌晨1点执行效期预警扫描。
     * 扫描所有库存记录，对距离过期日期不足90天的药品生成或更新预警记录。
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scanExpiryAlerts() {
        log.info("开始执行效期预警扫描定时任务...");
        try {
            drugExpiryAlertService.scanAndGenerateAlerts();
            log.info("效期预警扫描定时任务执行完成");
        } catch (Exception e) {
            log.error("效期预警扫描定时任务执行异常", e);
        }
    }
}
