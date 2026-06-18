package com.muGood.web.controller;

import com.muGood.common.api.Result;
import com.muGood.service.AuthService;
import com.muGood.service.dto.LoginRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/admin/login")
    public Result<Map<String, Object>> adminLogin(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.adminLogin(request));
    }
}
