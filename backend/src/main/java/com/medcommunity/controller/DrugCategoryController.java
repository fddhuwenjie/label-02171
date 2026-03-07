package com.medcommunity.controller;

import com.medcommunity.common.Result;
import com.medcommunity.entity.DrugCategory;
import com.medcommunity.service.DrugCategoryService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/drug-categories")
public class DrugCategoryController {

    @Autowired
    private DrugCategoryService drugCategoryService;

    @GetMapping("/tree")
    public Result<List<DrugCategory>> tree() {
        return Result.success(drugCategoryService.getCategoryTree());
    }

    @PostMapping
    public Result<DrugCategory> create(@Valid @RequestBody DrugCategory category) {
        drugCategoryService.save(category);
        return Result.success(category);
    }

    @PutMapping("/{id}")
    public Result<DrugCategory> update(@PathVariable Long id, @Valid @RequestBody DrugCategory category) {
        category.setId(id);
        drugCategoryService.updateById(category);
        return Result.success(category);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        drugCategoryService.removeById(id);
        return Result.success();
    }
}
