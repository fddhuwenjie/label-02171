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
     *
     * @param drugId 药品ID
     * @param hospitalId 机构ID
     * @param alertLevel 预警级别
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping
    public Result<IPage<DrugExpiryAlert>> page(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(drugExpiryAlertService.pageList(drugId, hospitalId, alertLevel, status, page, size));
    }

    /**
     * 查询活跃的预警列表
     *
     * @return 预警列表
     */
    @GetMapping("/active")
    public Result<List<DrugExpiryAlert>> activeAlerts() {
        return Result.success(drugExpiryAlertService.getActiveAlerts());
    }

    /**
     * 手动触发效期预警扫描
     *
     * @return 操作结果
     */
    @GetMapping("/scan")
    public Result<Void> scan() {
        drugExpiryAlertService.scanAndGenerateAlerts();
        return Result.success();
    }

    /**
     * 标记预警为已解决
     *
     * @param id 预警ID
     * @return 操作结果
     */
    @PutMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable Long id) {
        drugExpiryAlertService.resolveAlert(id);
        return Result.success();
    }
}
