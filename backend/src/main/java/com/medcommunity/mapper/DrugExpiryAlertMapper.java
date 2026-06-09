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
     * 分页查询效期预警列表，关联药品和机构名称。
     *
     * @param page 分页参数
     * @param status 预警状态
     * @return 分页结果
     */
    IPage<DrugExpiryAlert> selectAlertList(IPage<DrugExpiryAlert> page,
                                            @Param("status") String status);

    /**
     * 查询所有生效的效期预警记录。
     *
     * @return 生效预警列表
     */
    List<DrugExpiryAlert> selectActiveAlerts();

    /**
     * 根据库存记录ID查询生效预警。
     *
     * @param inventoryId 库存记录ID
     * @return 预警记录
     */
    DrugExpiryAlert selectActiveByInventoryId(@Param("inventoryId") Long inventoryId);
}
