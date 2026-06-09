package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.entity.DrugExpiryAlert;

import java.util.List;

/**
 * 药品效期预警 Service。
 */
public interface DrugExpiryAlertService {

    /**
     * 扫描所有库存记录，对距离过期不足 90 天的药品生成预警记录。
     * 由定时任务每日触发；接口暴露后也可手动触发。
     *
     * @return 本次扫描新增/更新的预警条数
     */
    int scanAndGenerateAlerts();

    /**
     * 分页查询效期预警列表。
     *
     * @param drugId      药品ID（可空）
     * @param hospitalId  机构ID（可空）
     * @param alertLevel  预警等级（可空）
     * @param status      处理状态（可空）
     * @param page        页码
     * @param size        每页条数
     * @return 分页结果
     */
    IPage<DrugExpiryAlert> pageList(Long drugId, Long hospitalId, String alertLevel, Integer status, int page, int size);

    /**
     * 数据看板使用：取最新的若干条未处理预警，用于红色标签展示。
     *
     * @param limit 数量上限
     * @return 预警列表
     */
    List<DrugExpiryAlert> getRecentAlerts(int limit);
}
