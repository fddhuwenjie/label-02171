package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.entity.Hospital;
import com.medcommunity.service.HospitalService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @GetMapping
    public Result<IPage<Hospital>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(hospitalService.pageList(keyword, status, page, size));
    }

    @GetMapping("/list")
    public Result<List<Hospital>> list() {
        return Result.success(hospitalService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Hospital> detail(@PathVariable Long id) {
        return Result.success(hospitalService.getById(id));
    }

    @PostMapping
    public Result<Hospital> create(@Valid @RequestBody Hospital hospital) {
        hospitalService.save(hospital);
        return Result.success(hospital);
    }

    @PutMapping("/{id}")
    public Result<Hospital> update(@PathVariable Long id, @Valid @RequestBody Hospital hospital) {
        hospital.setId(id);
        hospitalService.updateById(hospital);
        return Result.success(hospital);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        hospitalService.removeById(id);
        return Result.success();
    }
}
