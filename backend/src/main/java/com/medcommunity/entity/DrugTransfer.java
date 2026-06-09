package com.medcommunity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("drug_transfer")
public class DrugTransfer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String transferNo;

    private Long fromHospitalId;

    private Long toHospitalId;

    private String status;

    private String remark;

    /**
     * 驳回理由（仅当 status=REJECTED 时有值）。
     */
    private String rejectReason;

    private Long createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String fromHospitalName;

    @TableField(exist = false)
    private String toHospitalName;

    @TableField(exist = false)
    private String createdByName;

    @TableField(exist = false)
    private List<DrugTransferItem> items;
}
