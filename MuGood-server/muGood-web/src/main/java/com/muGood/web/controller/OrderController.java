package com.muGood.web.controller;

import com.muGood.common.api.Result;
import com.muGood.common.api.PageResult;
import com.muGood.service.OrderService;
import com.muGood.service.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/member/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/pre")
    public Result<Map<String, Object>> preOrder() {
        return Result.ok(orderService.preOrder());
    }

    @PostMapping
    public Result<Map<String, Object>> create(@Valid @RequestBody CreateOrderRequest request) {
        return Result.ok(orderService.create(request));
    }

    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(@RequestParam(required = false, defaultValue = "0") Integer orderState,
                                                        @RequestParam(required = false, defaultValue = "1") long page,
                                                        @RequestParam(required = false, defaultValue = "10") long pageSize) {
        return Result.ok(orderService.list(orderState, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(id));
    }
}
