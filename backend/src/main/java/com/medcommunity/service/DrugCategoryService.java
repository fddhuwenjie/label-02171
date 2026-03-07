package com.medcommunity.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medcommunity.entity.DrugCategory;

import java.util.List;

public interface DrugCategoryService extends IService<DrugCategory> {

    List<DrugCategory> getCategoryTree();
}
