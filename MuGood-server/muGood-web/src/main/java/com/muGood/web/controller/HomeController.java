package com.muGood.web.controller;

import com.muGood.common.api.Result;
import com.muGood.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HomeController {
    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/home/banner")
    public Result<List<Map<String, Object>>> banners(@RequestParam(required = false) Integer distributionSite) {
        return Result.ok(homeService.banners(distributionSite));
    }

    @GetMapping("/home/category/head")
    public Result<List<Map<String, Object>>> headCategories() {
        return Result.ok(homeService.headCategories());
    }

    @GetMapping("/home/new")
    public Result<List<Map<String, Object>>> newGoods() {
        return Result.ok(homeService.newGoods());
    }

    @GetMapping("/home/hot")
    public Result<List<Map<String, Object>>> hotGoods() {
        return Result.ok(homeService.hotGoods());
    }

    @GetMapping("/home/goods")
    public Result<List<Map<String, Object>>> productPanels() {
        return Result.ok(homeService.productPanels());
    }
}
