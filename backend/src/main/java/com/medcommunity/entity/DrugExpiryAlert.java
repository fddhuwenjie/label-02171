package com.medcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 药品效期预警实体。
 * 由定时任务每日扫描 {@link DrugInventory} 生成。
 */
@Data
@TableName("drug_expiry_alert")
public class DrugExpiryAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的库存记录ID */
    private Long inventoryId;

    private Long drugId;

    private Long hospitalId;

    private String batchNo;

    /** 过期日期 */
    private LocalDate expireDate;

    /** 距离过期剩余天数（生成时快照，可能为负数表示已过期） */
    private Integer daysToExpire;

    /** 预警等级 WARNING / CRITICAL / EXPIRED */
    private String alertLevel;

    /** 状态 0-已处理 1-未处理 */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String drugName;

    @TableField(exist = false)
    private String drugCode;

    @TableField(exist = false)
    private String spec;

    @TableField(exist = false)
    private String unit;

    @TableField(exist = false)
    private String hospitalName;
}
