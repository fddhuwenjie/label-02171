package com.medcommunity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.medcommunity.common.Result;
import com.medcommunity.entity.DrugInfo;
import com.medcommunity.service.DrugInfoService;
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
@RequestMapping("/api/drugs")
public class DrugInfoController {

    @Autowired
    private DrugInfoService drugInfoService;

    @GetMapping
    public Result<IPage<DrugInfo>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(drugInfoService.pageList(keyword, categoryId, status, page, size));
    }

    @GetMapping("/list")
    public Result<List<DrugInfo>> list() {
        return Result.success(drugInfoService.listAll());
    }

    @GetMapping("/{id}")
    public Result<DrugInfo> detail(@PathVariable Long id) {
        return Result.success(drugInfoService.getById(id));
    }

    @PostMapping
    public Result<DrugInfo> create(@Valid @RequestBody DrugInfo drugInfo) {
        drugInfoService.save(drugInfo);
        return Result.success(drugInfo);
    }

    @PutMapping("/{id}")
    public Result<DrugInfo> update(@PathVariable Long id, @Valid @RequestBody DrugInfo drugInfo) {
        drugInfo.setId(id);
        drugInfoService.updateById(drugInfo);
        return Result.success(drugInfo);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        drugInfoService.removeById(id);
        return Result.success();
    }
}
