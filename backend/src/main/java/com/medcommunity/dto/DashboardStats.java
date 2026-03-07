package com.medcommunity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {

    private long drugCount;

    private long hospitalCount;

    private long lowStockCount;

    private long pendingTransferCount;

    private long pendingPurchaseCount;
}
