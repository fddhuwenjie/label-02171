package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.DrugExpiryAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DrugExpiryAlertMapper extends BaseMapper<DrugExpiryAlert> {

    /**
     * 分页查询效期预警列表（关联药品、机构信息）
     */
    IPage<DrugExpiryAlert> selectAlertPage(IPage<DrugExpiryAlert> page,
                                           @Param("hospitalId") Long hospitalId,
                                           @Param("alertLevel") String alertLevel,
                                           @Param("status") String status);

    /**
     * 查询未处理的有效预警列表（用于Dashboard展示）
     */
    List<DrugExpiryAlert> selectActiveAlerts(@Param("limit") Integer limit);

    /**
     * 查询所有未处理的预警（供定时任务去重使用）
     */
    List<DrugExpiryAlert> selectAllActiveAlerts();

    /**
     * 统计未处理预警数量
     */
    long countActiveAlerts();
}
