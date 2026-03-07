package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.dto.InventoryAdjustRequest;
import com.medcommunity.entity.DrugInventory;
import com.medcommunity.entity.InventoryLog;
import com.medcommunity.exception.BusinessException;
import com.medcommunity.mapper.DrugInventoryMapper;
import com.medcommunity.mapper.DrugTransferItemMapper;
import com.medcommunity.mapper.InventoryLogMapper;
import com.medcommunity.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Autowired
    private DrugTransferItemMapper drugTransferItemMapper;

    @Override
    public IPage<DrugInventory> pageList(Long drugId, Long hospitalId, Boolean warningOnly, int page, int size) {
        Page<DrugInventory> pageParam = new Page<>(page, size);
        return drugInventoryMapper.selectInventoryList(pageParam, drugId, hospitalId, warningOnly);
    }

    @Override
    public List<DrugInventory> getWarnings() {
        return drugInventoryMapper.selectWarningList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(InventoryAdjustRequest request, String operator) {
        DrugInventory inventory = drugInventoryMapper.selectOne(
                new LambdaQueryWrapper<DrugInventory>()
                        .eq(DrugInventory::getDrugId, request.getDrugId())
                        .eq(DrugInventory::getHospitalId, request.getHospitalId())
        );

        if (inventory == null) {
            inventory = new DrugInventory();
            inventory.setDrugId(request.getDrugId());
            inventory.setHospitalId(request.getHospitalId());
            inventory.setQuantity(request.getAdjustQuantity());
            inventory.setBatchNo(request.getBatchNo());
            if (inventory.getQuantity() < 0) {
                throw new BusinessException("库存不足，无法调整");
            }
            drugInventoryMapper.insert(inventory);
        } else {
            int newQuantity = inventory.getQuantity() + request.getAdjustQuantity();
            if (newQuantity < 0) {
                throw new BusinessException("库存不足，无法调整");
            }
            inventory.setQuantity(newQuantity);
            if (request.getBatchNo() != null) {
                inventory.setBatchNo(request.getBatchNo());
            }
            drugInventoryMapper.updateById(inventory);
        }

        InventoryLog logEntry = new InventoryLog();
        logEntry.setDrugId(request.getDrugId());
        logEntry.setHospitalId(request.getHospitalId());
        logEntry.setType("ADJUST");
        logEntry.setQuantity(request.getAdjustQuantity());
        logEntry.setBatchNo(request.getBatchNo());
        logEntry.setOperator(operator);
        logEntry.setRemark(request.getRemark());
        inventoryLogMapper.insert(logEntry);

        log.info("库存调整完成: drugId={}, hospitalId={}, adjustQuantity={}", request.getDrugId(), request.getHospitalId(), request.getAdjustQuantity());
    }

    @Override
    public Integer getQuantityByDrugAndHospital(Long drugId, Long hospitalId) {
        Integer qty = drugInventoryMapper.sumQuantityByDrugAndHospital(drugId, hospitalId);
        return qty != null ? qty : 0;
    }

    @Override
    public Integer getAvailableQuantity(Long drugId, Long hospitalId, Long excludeTransferId) {
        Integer total = drugInventoryMapper.sumQuantityByDrugAndHospital(drugId, hospitalId);
        Integer reserved = drugTransferItemMapper.sumReservedQuantity(drugId, hospitalId, excludeTransferId != null ? excludeTransferId : -1L);
        int t = total != null ? total : 0;
        int r = reserved != null ? reserved : 0;
        return Math.max(0, t - r);
    }
}
