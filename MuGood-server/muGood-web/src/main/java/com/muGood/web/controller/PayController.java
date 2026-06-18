package com.muGood.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
public class PayController {
    private final JdbcTemplate jdbcTemplate;

    public PayController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/pay/aliPay")
    public void aliPay(@RequestParam Long orderId,
                       @RequestParam String redirect,
                       HttpServletResponse response) throws IOException {
        jdbcTemplate.update("""
                update orders
                set order_state = 2, paid_at = current_timestamp, updated_at = current_timestamp
                where id = ?
                """, orderId);
        String target = redirect + "?orderId=" + URLEncoder.encode(String.valueOf(orderId), StandardCharsets.UTF_8)
                + "&payResult=true";
        response.sendRedirect(target);
    }
}
