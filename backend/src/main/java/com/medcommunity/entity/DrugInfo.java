package com.medcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("drug_info")
public class DrugInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String genericName;

    private String code;

    private Long categoryId;

    private String manufacturer;

    private String spec;

    private String unit;

    private String dosageForm;

    private String approvalNumber;

    private BigDecimal price;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String categoryName;
}
