package com.muGood.web.controller;

import com.muGood.common.api.Result;
import com.muGood.service.CustomerAgentService;
import com.muGood.service.dto.CustomerAgentRequest;
import com.muGood.service.dto.CustomerAgentResponse;
import com.muGood.service.dto.CustomerAgentStreamEvent;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent/customer-service")
public class CustomerAgentController {
    private final CustomerAgentService customerAgentService;

    public CustomerAgentController(CustomerAgentService customerAgentService) {
        this.customerAgentService = customerAgentService;
    }

    @PostMapping("/chat")
    public Result<CustomerAgentResponse> chat(@RequestBody CustomerAgentRequest request) {
        return Result.ok(customerAgentService.chat(request.message()));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestBody CustomerAgentRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        customerAgentService.stream(request.sessionId(), request.message())
                .subscribe(
                        event -> send(emitter, event),
                        emitter::completeWithError,
                        emitter::complete
                );
        return emitter;
    }

    @GetMapping("/goods")
    public Result<List<Map<String, Object>>> goods(@RequestParam(required = false) Integer limit) {
        return Result.ok(customerAgentService.allGoods(limit));
    }

    private void send(SseEmitter emitter, CustomerAgentStreamEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event.type())
                    .data(event));
        } catch (IOException exception) {
            emitter.completeWithError(exception);
        }
    }
}
