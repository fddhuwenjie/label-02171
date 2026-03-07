package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.entity.InventoryLog;
import com.medcommunity.service.InventoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/inventory-logs")
public class InventoryLogController {

    @Autowired
    private InventoryLogService inventoryLogService;

    @GetMapping
    public Result<IPage<InventoryLog>> page(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(inventoryLogService.pageList(drugId, hospitalId, type, page, size));
    }
}
