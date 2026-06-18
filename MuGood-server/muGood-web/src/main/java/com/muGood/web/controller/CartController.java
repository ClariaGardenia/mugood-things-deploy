package com.muGood.web.controller;

import com.muGood.common.api.Result;
import com.muGood.service.CartService;
import com.muGood.service.dto.AddCartRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.ok(cartService.list());
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody AddCartRequest request) {
        cartService.add(request);
        return Result.ok(null);
    }

    @DeleteMapping
    public Result<Void> delete(@RequestBody Map<String, List<Long>> body) {
        cartService.delete(body.get("ids"));
        return Result.ok(null);
    }

    @PostMapping("/merge")
    public Result<Void> merge(@RequestBody List<Map<String, Object>> items) {
        cartService.merge(items);
        return Result.ok(null);
    }
}
