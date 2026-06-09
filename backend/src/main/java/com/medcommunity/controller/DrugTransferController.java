package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.dto.RejectRequest;
import com.medcommunity.dto.TransferRequest;
import com.medcommunity.entity.DrugTransfer;
import com.medcommunity.service.DrugTransferService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/transfers")
public class DrugTransferController {

    @Autowired
    private DrugTransferService drugTransferService;

    /**
     * 分页查询调拨单列表
     */
    @GetMapping
    public Result<IPage<DrugTransfer>> page(
            @RequestParam(required = false) String transferNo,
            @RequestParam(required = false) Long fromHospitalId,
            @RequestParam(required = false) Long toHospitalId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(drugTransferService.pageList(transferNo, fromHospitalId, toHospitalId, status, page, size));
    }

    /**
     * 获取调拨单详情
     */
    @GetMapping("/{id}")
    public Result<DrugTransfer> detail(@PathVariable Long id) {
        return Result.success(drugTransferService.getDetail(id));
    }

    /**
     * 创建调拨单
     */
    @PostMapping
    public Result<DrugTransfer> create(@Valid @RequestBody TransferRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        DrugTransfer transfer = drugTransferService.create(request, userId);
        return Result.success(transfer);
    }

    /**
     * 审批通过调拨单
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        drugTransferService.approve(id);
        return Result.success();
    }

    /**
     * 驳回调拨单（需填写驳回理由）
     */
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        drugTransferService.reject(id, request.getRejectReason());
        return Result.success();
    }

    /**
     * 调拨单发货
     */
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        drugTransferService.ship(id);
        return Result.success();
    }

    /**
     * 完成调拨单收货确认
     */
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        drugTransferService.complete(id, operator);
        return Result.success();
    }
}
