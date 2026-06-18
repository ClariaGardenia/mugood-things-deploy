package com.muGood.web.controller;

import com.muGood.common.api.PageResult;
import com.muGood.common.api.Result;
import com.muGood.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CategoryController {
    private final CatalogService catalogService;

    public CategoryController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/category")
    public Result<Map<String, Object>> category(@RequestParam Long id) {
        return Result.ok(catalogService.category(id));
    }

    @GetMapping("/category/sub/filter")
    public Result<Map<String, Object>> subFilter(@RequestParam Long id) {
        return Result.ok(catalogService.subFilter(id));
    }

    @PostMapping("/category/goods/temporary")
    public Result<PageResult<Map<String, Object>>> goods(@RequestBody Map<String, Object> body) {
        Long categoryId = longValue(body.getOrDefault("categoryId", body.get("categortId")));
        long page = longValue(body.getOrDefault("page", 1));
        long pageSize = longValue(body.getOrDefault("pageSize", 10));
        String sortField = String.valueOf(body.getOrDefault("sortField", "publishTime"));
        return Result.ok(catalogService.goods(categoryId, page, pageSize, sortField));
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}
