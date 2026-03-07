package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medcommunity.entity.DrugInfo;
import com.medcommunity.mapper.DrugInfoMapper;
import com.medcommunity.service.DrugInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class DrugInfoServiceImpl extends ServiceImpl<DrugInfoMapper, DrugInfo> implements DrugInfoService {

    @Override
    public IPage<DrugInfo> pageList(String keyword, Long categoryId, Integer status, int page, int size) {
        LambdaQueryWrapper<DrugInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(DrugInfo::getName, keyword)
                    .or().like(DrugInfo::getGenericName, keyword)
                    .or().like(DrugInfo::getCode, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(DrugInfo::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(DrugInfo::getStatus, status);
        }
        wrapper.orderByDesc(DrugInfo::getCreatedAt);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<DrugInfo> listAll() {
        return this.list(new LambdaQueryWrapper<DrugInfo>().eq(DrugInfo::getStatus, 1).orderByAsc(DrugInfo::getId));
    }
}
