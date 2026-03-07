package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.dto.InventoryAdjustRequest;
import com.medcommunity.entity.DrugInventory;

import java.util.List;

public interface InventoryService {

    IPage<DrugInventory> pageList(Long drugId, Long hospitalId, Boolean warningOnly, int page, int size);

    List<DrugInventory> getWarnings();

    void adjust(InventoryAdjustRequest request, String operator);

    Integer getQuantityByDrugAndHospital(Long drugId, Long hospitalId);

    Integer getAvailableQuantity(Long drugId, Long hospitalId, Long excludeTransferId);
}
