package com.medcommunity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PurchaseOrderRequest {

    @NotNull(message = "医院ID不能为空")
    private Long hospitalId;

    private String supplier;

    private String remark;

    @NotEmpty(message = "采购明细不能为空")
    private List<PurchaseOrderItemDTO> items;

    @Data
    public static class PurchaseOrderItemDTO {

        @NotNull(message = "药品ID不能为空")
        private Long drugId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        @NotNull(message = "单价不能为空")
        private BigDecimal unitPrice;
    }
}
