package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.dto.PurchaseOrderRequest;
import com.medcommunity.entity.PurchaseOrder;
import com.medcommunity.service.PurchaseOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public Result<IPage<PurchaseOrder>> page(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(purchaseOrderService.pageList(orderNo, hospitalId, status, page, size));
    }

    @GetMapping("/{id}")
    public Result<PurchaseOrder> detail(@PathVariable Long id) {
        return Result.success(purchaseOrderService.getDetail(id));
    }

    @PostMapping
    public Result<PurchaseOrder> create(@Valid @RequestBody PurchaseOrderRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        PurchaseOrder order = purchaseOrderService.create(request, userId);
        return Result.success(order);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        purchaseOrderService.approve(id);
        return Result.success();
    }

    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id, HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        purchaseOrderService.receive(id, operator);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.delete(id);
        return Result.success();
    }
}
