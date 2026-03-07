package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    IPage<PurchaseOrder> selectOrderList(IPage<PurchaseOrder> page,
                                        @Param("orderNo") String orderNo,
                                        @Param("hospitalId") Long hospitalId,
                                        @Param("status") String status);
}
