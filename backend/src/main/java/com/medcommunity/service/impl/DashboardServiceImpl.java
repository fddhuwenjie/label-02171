package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medcommunity.dto.DashboardStats;
import com.medcommunity.entity.DrugExpiryAlert;
import com.medcommunity.entity.DrugInfo;
import com.medcommunity.entity.DrugTransfer;
import com.medcommunity.entity.Hospital;
import com.medcommunity.entity.PurchaseOrder;
import com.medcommunity.mapper.DrugExpiryAlertMapper;
import com.medcommunity.mapper.DrugInfoMapper;
import com.medcommunity.mapper.DrugInventoryMapper;
import com.medcommunity.mapper.DrugTransferMapper;
import com.medcommunity.mapper.HospitalMapper;
import com.medcommunity.mapper.PurchaseOrderMapper;
import com.medcommunity.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private DrugInfoMapper drugInfoMapper;

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    @Autowired
    private DrugTransferMapper drugTransferMapper;

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private DrugExpiryAlertMapper drugExpiryAlertMapper;

    @Override
    public DashboardStats getStats() {
        long drugCount = drugInfoMapper.selectCount(
                new LambdaQueryWrapper<DrugInfo>().eq(DrugInfo::getStatus, 1)
        );
        long hospitalCount = hospitalMapper.selectCount(
                new LambdaQueryWrapper<Hospital>().eq(Hospital::getStatus, 1)
        );
        long lowStockCount = drugInventoryMapper.selectWarningList().size();
        long pendingTransferCount = drugTransferMapper.selectCount(
                new LambdaQueryWrapper<DrugTransfer>().eq(DrugTransfer::getStatus, "PENDING")
        );
        long pendingPurchaseCount = purchaseOrderMapper.selectCount(
                new LambdaQueryWrapper<PurchaseOrder>().eq(PurchaseOrder::getStatus, "PENDING")
        );
        long expiryAlertCount = drugExpiryAlertMapper.selectCount(
                new LambdaQueryWrapper<DrugExpiryAlert>().eq(DrugExpiryAlert::getStatus, "ACTIVE")
        );

        return new DashboardStats(drugCount, hospitalCount, lowStockCount, pendingTransferCount, pendingPurchaseCount, expiryAlertCount);
    }
}
