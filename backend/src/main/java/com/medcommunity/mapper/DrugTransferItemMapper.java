package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.medcommunity.entity.DrugTransferItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrugTransferItemMapper extends BaseMapper<DrugTransferItem> {

    @Select("SELECT dti.*, d.name AS drug_name, d.code AS drug_code, d.spec, d.unit " +
            "FROM drug_transfer_item dti " +
            "LEFT JOIN drug_info d ON dti.drug_id = d.id " +
            "WHERE dti.transfer_id = #{transferId}")
    List<DrugTransferItem> selectItemsByTransferId(@Param("transferId") Long transferId);

    @Select("SELECT COALESCE(SUM(dti.quantity), 0) FROM drug_transfer_item dti " +
            "JOIN drug_transfer dt ON dti.transfer_id = dt.id " +
            "WHERE dti.drug_id = #{drugId} AND dt.from_hospital_id = #{hospitalId} " +
            "AND dt.status IN ('APPROVED', 'SHIPPING') AND dt.id != #{excludeTransferId}")
    Integer sumReservedQuantity(@Param("drugId") Long drugId, @Param("hospitalId") Long hospitalId,
                                 @Param("excludeTransferId") Long excludeTransferId);
}
