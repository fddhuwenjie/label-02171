package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.entity.DrugExpiryAlert;

import java.util.List;

public interface DrugExpiryAlertService {

    /**
     * 分页查询效期预警列表
     */
    IPage<DrugExpiryAlert> pageList(Long hospitalId, String alertLevel, String status, int page, int size);

    /**
     * 获取活跃预警列表（未处理的）
     * @param limit 返回数量限制，null则返回全部
     */
    List<DrugExpiryAlert> getActiveAlerts(Integer limit);

    /**
     * 统计未处理预警数量
     */
    long countActiveAlerts();

    /**
     * 处理预警（标记为已处理）
     */
    void resolve(Long id);

    /**
     * 定时扫描所有库存，生成效期预警记录。
     * 每天扫描一次，距离过期不足90天的药品生成预警。
     * 对已有预警记录进行更新（过期天数变化、库存数量变化），对已过期或不再近效期的记录标记为RESOLVED。
     */
    void scanAndGenerateAlerts();
}
