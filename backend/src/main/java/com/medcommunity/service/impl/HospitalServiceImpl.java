package com.medcommunity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medcommunity.entity.Hospital;
import com.medcommunity.mapper.HospitalMapper;
import com.medcommunity.service.HospitalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital> implements HospitalService {

    @Override
    public IPage<Hospital> pageList(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Hospital::getName, keyword).or().like(Hospital::getCode, keyword));
        }
        if (status != null) {
            wrapper.eq(Hospital::getStatus, status);
        }
        wrapper.orderByDesc(Hospital::getCreatedAt);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Hospital> listAll() {
        return this.list(new LambdaQueryWrapper<Hospital>().eq(Hospital::getStatus, 1).orderByAsc(Hospital::getId));
    }
}
