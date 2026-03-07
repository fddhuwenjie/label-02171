package com.medcommunity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InventoryAdjustRequest {

    @NotNull(message = "药品ID不能为空")
    private Long drugId;

    @NotNull(message = "医院ID不能为空")
    private Long hospitalId;

    @NotNull(message = "调整数量不能为空")
    private Integer adjustQuantity;

    private String batchNo;

    private String remark;
}
