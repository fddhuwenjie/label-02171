package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.DrugInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrugInventoryMapper extends BaseMapper<DrugInventory> {

    IPage<DrugInventory> selectInventoryList(IPage<DrugInventory> page,
                                            @Param("drugId") Long drugId,
                                            @Param("hospitalId") Long hospitalId,
                                            @Param("warningOnly") Boolean warningOnly);

    @Select("SELECT di.*, d.name AS drug_name, d.code AS drug_code, d.spec, d.unit, h.name AS hospital_name " +
            "FROM drug_inventory di " +
            "LEFT JOIN drug_info d ON di.drug_id = d.id " +
            "LEFT JOIN hospital h ON di.hospital_id = h.id " +
            "WHERE di.quantity <= di.warning_threshold " +
            "ORDER BY di.quantity ASC")
    List<DrugInventory> selectWarningList();

    @Select("SELECT COALESCE(SUM(quantity), 0) FROM drug_inventory WHERE drug_id = #{drugId} AND hospital_id = #{hospitalId}")
    Integer sumQuantityByDrugAndHospital(@Param("drugId") Long drugId, @Param("hospitalId") Long hospitalId);
}
