package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.entity.DrugExpiryAlert;
import com.medcommunity.service.DrugExpiryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 药品效期预警控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/expiry-alerts")
public class DrugExpiryAlertController {

    @Autowired
    private DrugExpiryAlertService drugExpiryAlertService;

    /**
     * 分页查询效期预警列表。
     *
     * @param status 预警状态（ACTIVE/RESOLVED）
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping
    public Result<IPage<DrugExpiryAlert>> page(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(drugExpiryAlertService.pageList(status, page, size));
    }

    /**
     * 查询所有生效的效期预警记录。
     *
     * @return 生效预警列表
     */
    @GetMapping("/active")
    public Result<List<DrugExpiryAlert>> activeAlerts() {
        return Result.success(drugExpiryAlertService.getActiveAlerts());
    }

    /**
     * 手动触发效期预警扫描。
     *
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> scan() {
        drugExpiryAlertService.scanAndGenerateAlerts();
        return Result.success();
    }

    /**
     * 将指定预警标记为已处理。
     *
     * @param id 预警记录ID
     * @return 操作结果
     */
    @PutMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable Long id) {
        drugExpiryAlertService.resolve(id);
        return Result.success();
    }
}
