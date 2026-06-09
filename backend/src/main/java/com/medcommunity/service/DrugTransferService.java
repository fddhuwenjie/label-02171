package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.dto.TransferRequest;
import com.medcommunity.entity.DrugTransfer;

public interface DrugTransferService {

    /**
     * 分页查询调拨单列表
     * @param transferNo 调拨编号（模糊匹配）
     * @param fromHospitalId 调出机构ID
     * @param toHospitalId 调入机构ID
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<DrugTransfer> pageList(String transferNo, Long fromHospitalId, Long toHospitalId, String status, int page, int size);

    /**
     * 获取调拨单详情（含明细列表）
     * @param id 调拨单ID
     * @return 调拨单详情
     */
    DrugTransfer getDetail(Long id);

    /**
     * 创建调拨单（含效期检查：近效期30天内药品不可调拨）
     * @param request 创建请求
     * @param userId 创建人用户ID
     * @return 创建后的调拨单
     */
    DrugTransfer create(TransferRequest request, Long userId);

    /**
     * 审批通过调拨单
     * @param id 调拨单ID
     */
    void approve(Long id);

    /**
     * 驳回调拨单并记录驳回理由
     * @param id 调拨单ID
     * @param rejectReason 驳回理由
     */
    void reject(Long id, String rejectReason);

    /**
     * 调拨单发货（状态从APPROVED变为SHIPPING）
     * @param id 调拨单ID
     */
    void ship(Long id);

    /**
     * 完成调拨单收货确认，执行库存增减操作
     * @param id 调拨单ID
     * @param operator 操作人姓名
     */
    void complete(Long id, String operator);
}
