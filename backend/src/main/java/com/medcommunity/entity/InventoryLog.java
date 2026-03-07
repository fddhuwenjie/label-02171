package com.medcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("inventory_log")
public class InventoryLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long drugId;

    private Long hospitalId;

    private String type;

    private Integer quantity;

    private String batchNo;

    private String operator;

    private String remark;

    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String drugName;

    @TableField(exist = false)
    private String hospitalName;
}
