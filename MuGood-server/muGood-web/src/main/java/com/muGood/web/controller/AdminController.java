package com.muGood.web.controller;

import com.muGood.common.api.PageResult;
import com.muGood.common.api.Result;
import com.muGood.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        return Result.ok(adminService.summary());
    }

    @GetMapping("/goods")
    public Result<PageResult<Map<String, Object>>> goods(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long pageSize,
                                                         @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.goods(page, pageSize, keyword));
    }

    @GetMapping("/goods/{id}")
    public Result<Map<String, Object>> goodsDetail(@PathVariable Long id) {
        return Result.ok(adminService.goodsDetail(id));
    }

    @GetMapping("/options")
    public Result<Map<String, Object>> options() {
        return Result.ok(adminService.options());
    }

    @PostMapping("/goods")
    public Result<Map<String, Object>> createGoods(@RequestBody Map<String, Object> body) {
        return Result.ok(adminService.createGoods(body));
    }

    @PutMapping("/goods/{id}")
    public Result<Void> updateGoods(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminService.updateGoods(id, body);
        return Result.ok(null);
    }

    @DeleteMapping("/goods/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        adminService.deleteGoods(id);
        return Result.ok(null);
    }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.ok(adminService.categories());
    }

    @PostMapping("/categories")
    public Result<Map<String, Object>> createCategory(@RequestBody Map<String, Object> body) {
        return Result.ok(adminService.createCategory(body));
    }

    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminService.updateCategory(id, body);
        return Result.ok(null);
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.ok(null);
    }

    @GetMapping("/orders")
    public Result<PageResult<Map<String, Object>>> orders(@RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "10") long pageSize,
                                                          @RequestParam(required = false, defaultValue = "0") Integer orderState) {
        return Result.ok(adminService.orders(page, pageSize, orderState));
    }

    @PutMapping("/orders/{id}/state")
    public Result<Void> updateOrderState(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateOrderState(id, body.getOrDefault("orderState", 2));
        return Result.ok(null);
    }

    @GetMapping("/banners")
    public Result<List<Map<String, Object>>> banners() {
        return Result.ok(adminService.banners());
    }

    @PostMapping("/banners")
    public Result<Map<String, Object>> createBanner(@RequestBody Map<String, Object> body) {
        return Result.ok(adminService.createBanner(body));
    }

    @PutMapping("/banners/{id}")
    public Result<Void> updateBanner(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminService.updateBanner(id, body);
        return Result.ok(null);
    }

    @DeleteMapping("/banners/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        adminService.deleteBanner(id);
        return Result.ok(null);
    }

    @GetMapping("/users")
    public Result<PageResult<Map<String, Object>>> users(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long pageSize,
                                                         @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.users(page, pageSize, keyword));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateUserStatus(id, body.getOrDefault("status", 1));
        return Result.ok(null);
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> updateUserPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.updateUserPassword(id, body.getOrDefault("password", ""));
        return Result.ok(null);
    }

    @PutMapping("/goods/{id}/status")
    public Result<Void> updateGoodsStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        adminService.updateGoodsStatus(id, body.getOrDefault("status", 1));
        return Result.ok(null);
    }
}
