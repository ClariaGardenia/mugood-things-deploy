package com.muGood.service.dto;

import java.util.List;
import java.util.Map;

public record CustomerAgentStreamEvent(String type, String content, List<Map<String, Object>> goods) {
    public static CustomerAgentStreamEvent goods(List<Map<String, Object>> goods) {
        return new CustomerAgentStreamEvent("goods", null, goods);
    }

    public static CustomerAgentStreamEvent token(String content) {
        return new CustomerAgentStreamEvent("token", content, null);
    }

    public static CustomerAgentStreamEvent error(String content) {
        return new CustomerAgentStreamEvent("error", content, null);
    }

    public static CustomerAgentStreamEvent done() {
        return new CustomerAgentStreamEvent("done", null, null);
    }
}
