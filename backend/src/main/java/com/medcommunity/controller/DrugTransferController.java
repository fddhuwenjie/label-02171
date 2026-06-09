package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
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

    @GetMapping("/{id}")
    public Result<DrugTransfer> detail(@PathVariable Long id) {
        return Result.success(drugTransferService.getDetail(id));
    }

    @PostMapping
    public Result<DrugTransfer> create(@Valid @RequestBody TransferRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        DrugTransfer transfer = drugTransferService.create(request, userId);
        return Result.success(transfer);
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        drugTransferService.approve(id);
        return Result.success();
    }

    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestParam String rejectReason) {
        drugTransferService.reject(id, rejectReason);
        return Result.success();
    }

    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        drugTransferService.ship(id);
        return Result.success();
    }

    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, HttpServletRequest httpRequest) {
        String operator = (String) httpRequest.getAttribute("username");
        drugTransferService.complete(id, operator);
        return Result.success();
    }
}
