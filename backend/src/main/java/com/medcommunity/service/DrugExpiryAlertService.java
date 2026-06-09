package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.entity.DrugExpiryAlert;

import java.util.List;

public interface DrugExpiryAlertService {

    /**
     * 分页查询效期预警列表。
     *
     * @param status 预警状态
     * @param page 页码
     * @param size 每页大小
     * @return 分页结果
     */
    IPage<DrugExpiryAlert> pageList(String status, int page, int size);

    /**
     * 查询所有生效的效期预警记录。
     *
     * @return 生效预警列表
     */
    List<DrugExpiryAlert> getActiveAlerts();

    /**
     * 扫描库存并生成效期预警记录。
     * 对距离过期日期不足90天的药品生成预警，已处理的预警不重复生成。
     */
    void scanAndGenerateAlerts();

    /**
     * 将指定预警标记为已处理。
     *
     * @param id 预警记录ID
     */
    void resolve(Long id);
}
