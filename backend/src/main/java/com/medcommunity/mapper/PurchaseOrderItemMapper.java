package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medcommunity.entity.PurchaseOrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {

    @Select("SELECT poi.*, d.name AS drug_name, d.code AS drug_code, d.spec, d.unit " +
            "FROM purchase_order_item poi " +
            "LEFT JOIN drug_info d ON poi.drug_id = d.id " +
            "WHERE poi.order_id = #{orderId}")
    List<PurchaseOrderItem> selectItemsByOrderId(@Param("orderId") Long orderId);
}
