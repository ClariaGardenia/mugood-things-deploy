package com.muGood.web.controller;

import com.muGood.common.api.Result;
import com.muGood.service.GoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class GoodsController {
    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @GetMapping("/goods")
    public Result<Map<String, Object>> detail(@RequestParam Long id) {
        return Result.ok(goodsService.detail(id));
    }

    @GetMapping("/goods/hot")
    public Result<List<Map<String, Object>>> hot(@RequestParam Long id,
                                                 @RequestParam(required = false) Integer type,
                                                 @RequestParam(required = false) Integer limit) {
        return Result.ok(goodsService.hot(id, type, limit));
    }

    @GetMapping("/goods/relevant")
    public Result<List<Map<String, Object>>> relevant(@RequestParam(required = false) Integer limit) {
        return Result.ok(goodsService.relevant(limit));
    }
}
