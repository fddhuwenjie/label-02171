package com.medcommunity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferItemRequest {

    @NotNull
    private Long drugId;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String batchNo;
}
