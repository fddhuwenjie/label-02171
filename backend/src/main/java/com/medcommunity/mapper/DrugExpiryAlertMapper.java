package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.entity.DrugExpiryAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 药品效期预警 Mapper。
 */
@Mapper
public interface DrugExpiryAlertMapper extends BaseMapper<DrugExpiryAlert> {

    /**
     * 分页查询效期预警列表（关联药品 / 机构信息）。
     *
     * @param page        分页参数
     * @param drugId      药品ID（可空）
     * @param hospitalId  机构ID（可空）
     * @param alertLevel  预警等级（可空）
     * @param status      处理状态（可空）
     * @return 分页结果
     */
    IPage<DrugExpiryAlert> selectAlertPage(IPage<DrugExpiryAlert> page,
                                          @Param("drugId") Long drugId,
                                          @Param("hospitalId") Long hospitalId,
                                          @Param("alertLevel") String alertLevel,
                                          @Param("status") Integer status);

    /**
     * 仪表盘红标签使用：取最新的 N 条未处理预警。
     *
     * @param limit 数量上限
     * @return 预警列表
     */
    List<DrugExpiryAlert> selectRecentAlerts(@Param("limit") int limit);
}
