package com.medcommunity.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TransferRequest {

    @NotNull(message = "调出医院ID不能为空")
    private Long fromHospitalId;

    @NotNull(message = "调入医院ID不能为空")
    private Long toHospitalId;

    private String remark;

    @NotEmpty(message = "调拨明细不能为空")
    private List<TransferItemDTO> items;

    @Data
    public static class TransferItemDTO {

        @NotNull(message = "药品ID不能为空")
        private Long drugId;

        @NotNull(message = "数量不能为空")
        private Integer quantity;

        private String batchNo;
    }
}
