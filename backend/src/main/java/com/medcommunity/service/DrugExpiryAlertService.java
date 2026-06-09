package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.entity.DrugExpiryAlert;

import java.util.List;

public interface DrugExpiryAlertService {

    /**
     * 分页查询效期预警列表
     *
     * @param drugId 药品ID
     * @param hospitalId 机构ID
     * @param alertLevel 预警级别
     * @param status 状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<DrugExpiryAlert> pageList(Long drugId, Long hospitalId, String alertLevel, String status, int page, int size);

    /**
     * 查询活跃的预警列表
     *
     * @return 预警列表
     */
    List<DrugExpiryAlert> getActiveAlerts();

    /**
     * 扫描库存并生成效期预警
     */
    void scanAndGenerateAlerts();

    /**
     * 标记预警为已解决
     *
     * @param id 预警ID
     */
    void resolveAlert(Long id);

    /**
     * 检查药品批次是否近效期（不足30天）
     *
     * @param drugId 药品ID
     * @param hospitalId 机构ID
     * @param batchNo 批号
     * @return 是否近效期
     */
    boolean isNearExpiry(Long drugId, Long hospitalId, String batchNo);
}
