package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.entity.DrugExpiryAlert;
import com.medcommunity.service.DrugExpiryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/expiry-alerts")
public class DrugExpiryAlertController {

    @Autowired
    private DrugExpiryAlertService drugExpiryAlertService;

    /**
     * 分页查询效期预警列表
     */
    @GetMapping
    public Result<IPage<DrugExpiryAlert>> page(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(drugExpiryAlertService.pageList(hospitalId, alertLevel, status, page, size));
    }

    /**
     * 获取活跃效期预警（用于Dashboard展示）
     */
    @GetMapping("/active")
    public Result<List<DrugExpiryAlert>> active(@RequestParam(required = false, defaultValue = "20") Integer limit) {
        return Result.success(drugExpiryAlertService.getActiveAlerts(limit));
    }

    /**
     * 获取未处理预警数量
     */
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(drugExpiryAlertService.countActiveAlerts());
    }

    /**
     * 处理预警（标记为已处理）
     */
    @PutMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable Long id) {
        drugExpiryAlertService.resolve(id);
        return Result.success();
    }

    /**
     * 手动触发扫描（便于调试，实际使用定时任务）
     */
    @PutMapping("/scan")
    public Result<Void> scan() {
        drugExpiryAlertService.scanAndGenerateAlerts();
        return Result.success();
    }
}
