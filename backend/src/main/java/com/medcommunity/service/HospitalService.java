package com.medcommunity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medcommunity.entity.Hospital;

import java.util.List;

public interface HospitalService extends IService<Hospital> {

    IPage<Hospital> pageList(String keyword, Integer status, int page, int size);

    List<Hospital> listAll();
}
