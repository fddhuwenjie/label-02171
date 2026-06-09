package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.dto.TransferRequest;
import com.medcommunity.entity.DrugInventory;
import com.medcommunity.entity.DrugTransfer;
import com.medcommunity.entity.DrugTransferItem;
import com.medcommunity.entity.InventoryLog;
import com.medcommunity.exception.BusinessException;
import com.medcommunity.service.InventoryService;
import com.medcommunity.mapper.DrugInventoryMapper;
import com.medcommunity.mapper.DrugTransferItemMapper;
import com.medcommunity.mapper.DrugTransferMapper;
import com.medcommunity.mapper.InventoryLogMapper;
import com.medcommunity.service.DrugTransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class DrugTransferServiceImpl implements DrugTransferService {

    @Autowired
    private DrugTransferMapper drugTransferMapper;

    @Autowired
    private DrugTransferItemMapper drugTransferItemMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DrugExpiryAlertService drugExpiryAlertService;

    @Override
    public IPage<DrugTransfer> pageList(String transferNo, Long fromHospitalId, Long toHospitalId, String status, int page, int size) {
        Page<DrugTransfer> pageParam = new Page<>(page, size);
        return drugTransferMapper.selectTransferList(pageParam, transferNo, fromHospitalId, toHospitalId, status);
    }

    @Override
    public DrugTransfer getDetail(Long id) {
        DrugTransfer transfer = drugTransferMapper.selectById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        List<DrugTransferItem> items = drugTransferItemMapper.selectItemsByTransferId(id);
        transfer.setItems(items);
        return transfer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransfer create(TransferRequest request, Long userId) {
        for (TransferRequest.TransferItemDTO itemDTO : request.getItems()) {
            if (itemDTO.getBatchNo() != null && !itemDTO.getBatchNo().isEmpty()) {
                boolean nearExpiry = drugExpiryAlertService.isNearExpiry(
                        itemDTO.getDrugId(),
                        request.getFromHospitalId(),
                        itemDTO.getBatchNo()
                );
                if (nearExpiry) {
                    throw new BusinessException("近效期药品不可调拨，药品ID：" + itemDTO.getDrugId() + "，批号：" + itemDTO.getBatchNo());
                }
            }
        }

        DrugTransfer transfer = new DrugTransfer();
        transfer.setTransferNo(generateTransferNo());
        transfer.setFromHospitalId(request.getFromHospitalId());
        transfer.setToHospitalId(request.getToHospitalId());
        transfer.setRemark(request.getRemark());
        transfer.setStatus("PENDING");
        transfer.setCreatedBy(userId);
        drugTransferMapper.insert(transfer);

        for (TransferRequest.TransferItemDTO itemDTO : request.getItems()) {
            DrugTransferItem item = new DrugTransferItem();
            item.setTransferId(transfer.getId());
            item.setDrugId(itemDTO.getDrugId());
            item.setQuantity(itemDTO.getQuantity());
            item.setBatchNo(itemDTO.getBatchNo());
            drugTransferItemMapper.insert(item);
        }

        log.info("创建调拨单: transferNo={}", transfer.getTransferNo());
        return transfer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        DrugTransfer transfer = drugTransferMapper.selectById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (!"PENDING".equals(transfer.getStatus())) {
            throw new BusinessException("只有待审批的调拨单才能审批");
        }
        List<DrugTransferItem> items = drugTransferItemMapper.selectList(
                new LambdaQueryWrapper<DrugTransferItem>().eq(DrugTransferItem::getTransferId, id)
        );
        for (DrugTransferItem item : items) {
            Integer totalStock = inventoryService.getQuantityByDrugAndHospital(item.getDrugId(), transfer.getFromHospitalId());
            Integer reserved = drugTransferItemMapper.sumReservedQuantity(item.getDrugId(), transfer.getFromHospitalId(), id);
            int available = (totalStock != null ? totalStock : 0) - (reserved != null ? reserved : 0);
            if (available < item.getQuantity()) {
                throw new BusinessException("调出机构库存不足，药品ID：" + item.getDrugId() + "，需要：" + item.getQuantity() + "，可用（含预留）：" + available);
            }
        }
        transfer.setStatus("APPROVED");
        drugTransferMapper.updateById(transfer);
        log.info("审批调拨单: transferNo={}", transfer.getTransferNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String rejectReason) {
        DrugTransfer transfer = drugTransferMapper.selectById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (!"PENDING".equals(transfer.getStatus())) {
            throw new BusinessException("只有待审批的调拨单才能驳回");
        }
        transfer.setStatus("REJECTED");
        transfer.setRejectReason(rejectReason);
        drugTransferMapper.updateById(transfer);
        log.info("驳回调拨单: transferNo={}, rejectReason={}", transfer.getTransferNo(), rejectReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(Long id) {
        DrugTransfer transfer = drugTransferMapper.selectById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (!"APPROVED".equals(transfer.getStatus())) {
            throw new BusinessException("只有已审批的调拨单才能发货");
        }
        transfer.setStatus("SHIPPING");
        drugTransferMapper.updateById(transfer);
        log.info("调拨单发货: transferNo={}", transfer.getTransferNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id, String operator) {
        DrugTransfer transfer = drugTransferMapper.selectById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (!"SHIPPING".equals(transfer.getStatus())) {
            throw new BusinessException("只有发货中的调拨单才能完成");
        }
        transfer.setStatus("COMPLETED");
        drugTransferMapper.updateById(transfer);

        List<DrugTransferItem> items = drugTransferItemMapper.selectList(
                new LambdaQueryWrapper<DrugTransferItem>().eq(DrugTransferItem::getTransferId, id)
        );

        for (DrugTransferItem item : items) {
            // 扣减调出方库存
            DrugInventory fromInventory = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, item.getDrugId())
                            .eq(DrugInventory::getHospitalId, transfer.getFromHospitalId())
            );
            if (fromInventory == null || fromInventory.getQuantity() < item.getQuantity()) {
                throw new BusinessException("调出方库存不足，药品ID：" + item.getDrugId());
            }
            fromInventory.setQuantity(fromInventory.getQuantity() - item.getQuantity());
            drugInventoryMapper.updateById(fromInventory);

            // 增加调入方库存
            DrugInventory toInventory = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, item.getDrugId())
                            .eq(DrugInventory::getHospitalId, transfer.getToHospitalId())
            );
            if (toInventory == null) {
                toInventory = new DrugInventory();
                toInventory.setDrugId(item.getDrugId());
                toInventory.setHospitalId(transfer.getToHospitalId());
                toInventory.setQuantity(item.getQuantity());
                toInventory.setBatchNo(item.getBatchNo());
                drugInventoryMapper.insert(toInventory);
            } else {
                toInventory.setQuantity(toInventory.getQuantity() + item.getQuantity());
                drugInventoryMapper.updateById(toInventory);
            }

            // 调出方库存日志
            InventoryLog outLog = new InventoryLog();
            outLog.setDrugId(item.getDrugId());
            outLog.setHospitalId(transfer.getFromHospitalId());
            outLog.setType("TRANSFER_OUT");
            outLog.setQuantity(-item.getQuantity());
            outLog.setBatchNo(item.getBatchNo());
            outLog.setOperator(operator);
            outLog.setRemark("调拨出库，调拨单号：" + transfer.getTransferNo());
            inventoryLogMapper.insert(outLog);

            // 调入方库存日志
            InventoryLog inLog = new InventoryLog();
            inLog.setDrugId(item.getDrugId());
            inLog.setHospitalId(transfer.getToHospitalId());
            inLog.setType("TRANSFER_IN");
            inLog.setQuantity(item.getQuantity());
            inLog.setBatchNo(item.getBatchNo());
            inLog.setOperator(operator);
            inLog.setRemark("调拨入库，调拨单号：" + transfer.getTransferNo());
            inventoryLogMapper.insert(inLog);
        }

        log.info("调拨单完成: transferNo={}", transfer.getTransferNo());
    }

    private String generateTransferNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000;
        return "TR" + timestamp + random;
    }
}
