package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.InventoryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryLogMapper extends BaseMapper<InventoryLog> {

    IPage<InventoryLog> selectLogList(IPage<InventoryLog> page,
                                     @Param("drugId") Long drugId,
                                     @Param("hospitalId") Long hospitalId,
                                     @Param("type") String type);
}
