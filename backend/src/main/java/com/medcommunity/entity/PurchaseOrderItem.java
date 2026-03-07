package com.medcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("purchase_order_item")
public class PurchaseOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long drugId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    @TableField(exist = false)
    private String drugName;

    @TableField(exist = false)
    private String drugCode;

    @TableField(exist = false)
    private String spec;

    @TableField(exist = false)
    private String unit;
}
