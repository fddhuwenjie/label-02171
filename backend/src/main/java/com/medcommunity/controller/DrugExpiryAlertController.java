package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.entity.DrugExpiryAlert;
import com.medcommunity.service.DrugExpiryAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 药品效期预警 Controller。
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
     * @param drugId      药品ID
     * @param hospitalId  机构ID
     * @param alertLevel  预警等级
     * @param status      处理状态
     * @param page        页码
     * @param size        每页条数
     * @return 分页结果
     */
    @GetMapping
    public Result<IPage<DrugExpiryAlert>> page(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(drugExpiryAlertService.pageList(drugId, hospitalId, alertLevel, status, page, size));
    }

    /**
     * 获取最近若干条未处理预警，用于数据看板红色标签展示。
     *
     * @param limit 数量上限，默认 10
     * @return 预警列表
     */
    @GetMapping("/recent")
    public Result<List<DrugExpiryAlert>> recent(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(drugExpiryAlertService.getRecentAlerts(limit));
    }

    /**
     * 手动触发一次扫描（管理员调试用）。
     *
     * @return 处理条数
     */
    @PostMapping("/scan")
    public Result<Integer> scan() {
        return Result.success(drugExpiryAlertService.scanAndGenerateAlerts());
    }
}
