package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.InventoryLog;
import com.medcommunity.mapper.InventoryLogMapper;
import com.medcommunity.service.InventoryLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventoryLogServiceImpl implements InventoryLogService {

    @Autowired
    private InventoryLogMapper inventoryLogMapper;

    @Override
    public IPage<InventoryLog> pageList(Long drugId, Long hospitalId, String type, int page, int size) {
        Page<InventoryLog> pageParam = new Page<>(page, size);
        return inventoryLogMapper.selectLogList(pageParam, drugId, hospitalId, type);
    }
}
