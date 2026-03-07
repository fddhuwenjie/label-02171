package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.dto.InventoryAdjustRequest;
import com.medcommunity.entity.DrugInventory;
import com.medcommunity.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public Result<IPage<DrugInventory>> page(
            @RequestParam(required = false) Long drugId,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Boolean warningOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(inventoryService.pageList(drugId, hospitalId, warningOnly, page, size));
    }

    @GetMapping("/warnings")
    public Result<List<DrugInventory>> warnings() {
        return Result.success(inventoryService.getWarnings());
    }

    @GetMapping("/quantity")
    public Result<Integer> quantity(
            @RequestParam Long drugId,
            @RequestParam Long hospitalId) {
        return Result.success(inventoryService.getQuantityByDrugAndHospital(drugId, hospitalId));
    }

    @GetMapping("/available")
    public Result<Integer> available(
            @RequestParam Long drugId,
            @RequestParam Long hospitalId,
            @RequestParam(required = false) Long excludeTransferId) {
        return Result.success(inventoryService.getAvailableQuantity(drugId, hospitalId, excludeTransferId));
    }

    @PostMapping("/adjust")
    public Result<Void> adjust(@Valid @RequestBody InventoryAdjustRequest request, HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        inventoryService.adjust(request, operator);
        return Result.success();
    }
}
