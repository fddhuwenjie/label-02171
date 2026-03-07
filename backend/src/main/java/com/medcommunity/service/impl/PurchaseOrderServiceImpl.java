package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.dto.PurchaseOrderRequest;
import com.medcommunity.entity.DrugInventory;
import com.medcommunity.entity.InventoryLog;
import com.medcommunity.entity.PurchaseOrder;
import com.medcommunity.entity.PurchaseOrderItem;
import com.medcommunity.exception.BusinessException;
import com.medcommunity.mapper.DrugInventoryMapper;
import com.medcommunity.mapper.InventoryLogMapper;
import com.medcommunity.mapper.PurchaseOrderItemMapper;
import com.medcommunity.mapper.PurchaseOrderMapper;
import com.medcommunity.service.PurchaseOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private DrugInventoryMapper drugInventoryMapper;

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    public IPage<PurchaseOrder> pageList(String orderNo, Long hospitalId, String status, int page, int size) {
        Page<PurchaseOrder> pageParam = new Page<>(page, size);
        return purchaseOrderMapper.selectOrderList(pageParam, orderNo, hospitalId, status);
    }

    @Override
    public PurchaseOrder getDetail(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectItemsByOrderId(id);
        order.setItems(items);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PurchaseOrder create(PurchaseOrderRequest request, Long userId) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(generateOrderNo());
        order.setHospitalId(request.getHospitalId());
        order.setSupplier(request.getSupplier());
        order.setRemark(request.getRemark());
        order.setStatus("PENDING");
        order.setCreatedBy(userId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderRequest.PurchaseOrderItemDTO itemDTO : request.getItems()) {
            BigDecimal amount = itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(amount);
        }
        order.setTotalAmount(totalAmount);
        purchaseOrderMapper.insert(order);

        for (PurchaseOrderRequest.PurchaseOrderItemDTO itemDTO : request.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setOrderId(order.getId());
            item.setDrugId(itemDTO.getDrugId());
            item.setQuantity(itemDTO.getQuantity());
            item.setUnitPrice(itemDTO.getUnitPrice());
            item.setAmount(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
            purchaseOrderItemMapper.insert(item);
        }

        log.info("创建采购订单: orderNo={}", order.getOrderNo());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只有待审批的订单才能审批");
        }
        order.setStatus("APPROVED");
        purchaseOrderMapper.updateById(order);
        log.info("审批采购订单: orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long id, String operator) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"APPROVED".equals(order.getStatus())) {
            throw new BusinessException("只有已审批的订单才能收货");
        }
        order.setStatus("COMPLETED");
        purchaseOrderMapper.updateById(order);

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id)
        );

        for (PurchaseOrderItem item : items) {
            DrugInventory inventory = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, item.getDrugId())
                            .eq(DrugInventory::getHospitalId, order.getHospitalId())
            );

            if (inventory == null) {
                inventory = new DrugInventory();
                inventory.setDrugId(item.getDrugId());
                inventory.setHospitalId(order.getHospitalId());
                inventory.setQuantity(item.getQuantity());
                drugInventoryMapper.insert(inventory);
            } else {
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                drugInventoryMapper.updateById(inventory);
            }

            InventoryLog logEntry = new InventoryLog();
            logEntry.setDrugId(item.getDrugId());
            logEntry.setHospitalId(order.getHospitalId());
            logEntry.setType("IN");
            logEntry.setQuantity(item.getQuantity());
            logEntry.setOperator(operator);
            logEntry.setRemark("采购入库，订单号：" + order.getOrderNo());
            inventoryLogMapper.insert(logEntry);
        }

        log.info("采购订单收货完成: orderNo={}", order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("采购订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("只有待审批的订单才能删除");
        }
        purchaseOrderItemMapper.delete(
                new LambdaQueryWrapper<PurchaseOrderItem>().eq(PurchaseOrderItem::getOrderId, id)
        );
        purchaseOrderMapper.deleteById(id);
        log.info("删除采购订单: orderNo={}", order.getOrderNo());
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = new Random().nextInt(9000) + 1000;
        return "PO" + timestamp + random;
    }
}
