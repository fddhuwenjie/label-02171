package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medcommunity.entity.DrugInfo;

import java.util.List;

public interface DrugInfoService extends IService<DrugInfo> {

    IPage<DrugInfo> pageList(String keyword, Long categoryId, Integer status, int page, int size);

    List<DrugInfo> listAll();
}
