package com.medcommunity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medcommunity.entity.DrugTransfer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DrugTransferMapper extends BaseMapper<DrugTransfer> {

    IPage<DrugTransfer> selectTransferList(IPage<DrugTransfer> page,
                                          @Param("transferNo") String transferNo,
                                          @Param("fromHospitalId") Long fromHospitalId,
                                          @Param("toHospitalId") Long toHospitalId,
                                          @Param("status") String status);
}
