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
     * 分页查询效期预警列表
     *
     * @param page 分页参数
     * @param drugId 药品ID
     * @param hospitalId 机构ID
     * @param alertLevel 预警级别
     * @param status 状态
     * @return 分页结果
     */
    IPage<DrugExpiryAlert> selectAlertList(IPage<DrugExpiryAlert> page,
                                           @Param("drugId") Long drugId,
                                           @Param("hospitalId") Long hospitalId,
                                           @Param("alertLevel") String alertLevel,
                                           @Param("status") String status);

    /**
     * 查询活跃的预警列表
     *
     * @return 预警列表
     */
    List<DrugExpiryAlert> selectActiveAlerts();

    /**
     * 根据库存ID查询活跃预警
     *
     * @param inventoryId 库存ID
     * @return 预警记录
     */
    DrugExpiryAlert selectActiveByInventoryId(@Param("inventoryId") Long inventoryId);
}
