package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.dto.TransferRequest;
import com.medcommunity.entity.DrugTransfer;

public interface DrugTransferService {

    IPage<DrugTransfer> pageList(String transferNo, Long fromHospitalId, Long toHospitalId, String status, int page, int size);

    DrugTransfer getDetail(Long id);

    DrugTransfer create(TransferRequest request, Long userId);

    void approve(Long id);

    /**
     * 驳回调拨单
     *
     * @param id 调拨单ID
     * @param rejectReason 驳回理由
     */
    void reject(Long id, String rejectReason);

    void ship(Long id);

    void complete(Long id, String operator);
}
