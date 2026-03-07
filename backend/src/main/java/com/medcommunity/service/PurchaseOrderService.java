package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.dto.PurchaseOrderRequest;
import com.medcommunity.entity.PurchaseOrder;

public interface PurchaseOrderService {

    IPage<PurchaseOrder> pageList(String orderNo, Long hospitalId, String status, int page, int size);

    PurchaseOrder getDetail(Long id);

    PurchaseOrder create(PurchaseOrderRequest request, Long userId);

    void approve(Long id);

    void receive(Long id, String operator);

    void delete(Long id);
}
