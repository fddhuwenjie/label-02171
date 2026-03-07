package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medcommunity.entity.DrugCategory;
import com.medcommunity.mapper.DrugCategoryMapper;
import com.medcommunity.service.DrugCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DrugCategoryServiceImpl extends ServiceImpl<DrugCategoryMapper, DrugCategory> implements DrugCategoryService {

    @Override
    public List<DrugCategory> getCategoryTree() {
        List<DrugCategory> allCategories = this.list(
                new LambdaQueryWrapper<DrugCategory>().orderByAsc(DrugCategory::getSortOrder)
        );

        Map<Long, List<DrugCategory>> parentMap = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() != 0)
                .collect(Collectors.groupingBy(DrugCategory::getParentId));

        List<DrugCategory> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());

        for (DrugCategory root : rootCategories) {
            root.setChildren(buildChildren(root.getId(), parentMap));
        }

        return rootCategories;
    }

    private List<DrugCategory> buildChildren(Long parentId, Map<Long, List<DrugCategory>> parentMap) {
        List<DrugCategory> children = parentMap.getOrDefault(parentId, new ArrayList<>());
        for (DrugCategory child : children) {
            child.setChildren(buildChildren(child.getId(), parentMap));
        }
        return children;
    }
}
