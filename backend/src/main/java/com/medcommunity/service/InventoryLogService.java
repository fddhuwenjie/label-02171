package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.entity.InventoryLog;

public interface InventoryLogService {

    IPage<InventoryLog> pageList(Long drugId, Long hospitalId, String type, int page, int size);
}
