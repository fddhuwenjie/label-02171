package com.medcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("drug_transfer_item")
public class DrugTransferItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long transferId;

    private Long drugId;

    private Integer quantity;

    private String batchNo;

    @TableField(exist = false)
    private String drugName;

    @TableField(exist = false)
    private String drugCode;

    @TableField(exist = false)
    private String spec;

    @TableField(exist = false)
    private String unit;
}
