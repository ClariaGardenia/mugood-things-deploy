package com.muGood.service.dto;

import java.util.List;
import java.util.Map;

public record CustomerAgentResponse(String answer, List<Map<String, Object>> goods) {
}
