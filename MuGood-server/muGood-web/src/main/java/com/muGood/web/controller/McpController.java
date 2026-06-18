package com.muGood.web.controller;

import com.muGood.service.CustomerAgentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {
    private final CustomerAgentService customerAgentService;

    public McpController(CustomerAgentService customerAgentService) {
        this.customerAgentService = customerAgentService;
    }

    @GetMapping("/tools")
    public Map<String, Object> tools() {
        return Map.of("tools", List.of(Map.of(
                "name", "muGoodListGoods",
                "description", "List products from the MuGood mall database.",
                "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of("limit", Map.of("type", "integer", "description", "Maximum products to return"))
                )
        )));
    }

    @PostMapping("/tools/muGoodListGoods")
    public Map<String, Object> listGoods(@RequestBody(required = false) Map<String, Object> body) {
        Integer limit = null;
        if (body != null && body.get("limit") instanceof Number number) {
            limit = number.intValue();
        }
        return Map.of("content", customerAgentService.allGoods(limit));
    }
}
