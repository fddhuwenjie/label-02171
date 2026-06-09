package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.dto.TransferRequest;
import com.medcommunity.entity.DrugInfo;
import com.medcommunity.entity.DrugInventory;
import com.medcommunity.entity.DrugTransfer;
import com.medcommunity.entity.DrugTransferItem;
import com.medcommunity.entity.InventoryLog;
import com.medcommunity.exception.BusinessException;
import com.medcommunity.mapper.DrugInfoMapper;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class DrugTransferServiceImpl implements DrugTransferService {

    private static final int NEAR_EXPIRY_DAYS_FOR_TRANSFER = 30;

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
    private DrugInfoMapper drugInfoMapper;

    /**
     * 分页查询调拨单列表（含机构名称关联）
     */
    @Override
    public IPage<DrugTransfer> pageList(String transferNo, Long fromHospitalId, Long toHospitalId, String status, int page, int size) {
        Page<DrugTransfer> pageParam = new Page<>(page, size);
        return drugTransferMapper.selectTransferList(pageParam, transferNo, fromHospitalId, toHospitalId, status);
    }

    /**
     * 获取调拨单详情，包含明细列表
     */
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

    /**
     * 创建调拨单。
     * 创建前校验调出药品批次效期：距离过期日期不足30天的药品不允许调拨，自动拒绝并提示。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DrugTransfer create(TransferRequest request, Long userId) {
        for (TransferRequest.TransferItemDTO itemDTO : request.getItems()) {
            DrugInventory inventory = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, itemDTO.getDrugId())
                            .eq(DrugInventory::getHospitalId, request.getFromHospitalId())
                            .eq(itemDTO.getBatchNo() != null, DrugInventory::getBatchNo, itemDTO.getBatchNo())
            );
            if (inventory != null && inventory.getExpireDate() != null) {
                LocalDate today = LocalDate.now();
                LocalDate expireDate = inventory.getExpireDate();
                long daysToExpire = java.time.temporal.ChronoUnit.DAYS.between(today, expireDate);
                if (!expireDate.isBefore(today) && daysToExpire < NEAR_EXPIRY_DAYS_FOR_TRANSFER) {
                    DrugInfo drugInfo = drugInfoMapper.selectById(itemDTO.getDrugId());
                    String drugName = drugInfo != null ? drugInfo.getName() : String.valueOf(itemDTO.getDrugId());
                    throw new BusinessException("近效期药品不可调拨：「" + drugName + "」将于 " + expireDate + " 过期，距过期仅剩 " + daysToExpire + " 天");
                }
                if (expireDate.isBefore(today)) {
                    DrugInfo drugInfo = drugInfoMapper.selectById(itemDTO.getDrugId());
                    String drugName = drugInfo != null ? drugInfo.getName() : String.valueOf(itemDTO.getDrugId());
                    throw new BusinessException("近效期药品不可调拨：「" + drugName + "」已于 " + expireDate + " 过期");
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

    /**
     * 审批通过调拨单，审批时校验库存可用量（含预留量扣减）
     */
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

    /**
     * 驳回调拨单，记录驳回理由
     * @param id 调拨单ID
     * @param rejectReason 驳回理由，不能为空
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String rejectReason) {
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new BusinessException("驳回理由不能为空");
        }
        DrugTransfer transfer = drugTransferMapper.selectById(id);
        if (transfer == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (!"PENDING".equals(transfer.getStatus())) {
            throw new BusinessException("只有待审批的调拨单才能驳回");
        }
        transfer.setStatus("REJECTED");
        transfer.setRejectReason(rejectReason.trim());
        drugTransferMapper.updateById(transfer);
        log.info("驳回调拨单: transferNo={}, reason={}", transfer.getTransferNo(), rejectReason);
    }

    /**
     * 调拨单发货（APPROVED → SHIPPING）
     */
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

    /**
     * 完成调拨单收货确认，执行调出方出库、调入方入库操作
     */
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

            InventoryLog outLog = new InventoryLog();
            outLog.setDrugId(item.getDrugId());
            outLog.setHospitalId(transfer.getFromHospitalId());
            outLog.setType("TRANSFER_OUT");
            outLog.setQuantity(-item.getQuantity());
            outLog.setBatchNo(item.getBatchNo());
            outLog.setOperator(operator);
            outLog.setRemark("调拨出库，调拨单号：" + transfer.getTransferNo());
            inventoryLogMapper.insert(outLog);

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
