package com.medcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("drug_inventory")
public class DrugInventory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long drugId;

    private Long hospitalId;

    private Integer quantity;

    private Integer warningThreshold;

    private String batchNo;

    private LocalDate expireDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String drugName;

    @TableField(exist = false)
    private String hospitalName;

    @TableField(exist = false)
    private String drugCode;

    @TableField(exist = false)
    private String spec;

    @TableField(exist = false)
    private String unit;
}
