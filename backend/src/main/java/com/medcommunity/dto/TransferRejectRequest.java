package com.medcommunity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 驳回调拨单请求体。
 */
@Data
public class TransferRejectRequest {

    /** 驳回理由（必填，最多 500 字） */
    @NotBlank(message = "驳回理由不能为空")
    @Size(max = 500, message = "驳回理由长度不能超过 500 字")
    private String rejectReason;
}
