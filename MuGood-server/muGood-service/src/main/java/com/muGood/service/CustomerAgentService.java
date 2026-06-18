package com.muGood.service;

import com.muGood.service.dto.CustomerAgentResponse;
import com.muGood.service.dto.CustomerAgentStreamEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerAgentService {
    private static final int MAX_MEMORY_ITEMS = 12;
    private static final Pattern ORDER_NO_PATTERN = Pattern.compile("(?i)FR\\d+");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final String SYSTEM_PROMPT = """
            You are MuGood mall's shopping assistant.
            Use recent conversation memory to answer direct profile questions first, such as height, gender, budget, style, or previous needs.
            You must use the provided database product context and never invent product ids, prices, links, inventory, or order status.
            If the user asks about an order number, call queryOrderByOrderNo and answer only from the returned order data.
            If the user asks for product recommendations and product context is empty, say there are no matching products in the current mall database.
            Give concise Chinese shopping advice. Mention product cards will appear after the answer and can be clicked to buy only when recommending products.
            """;

    private final Optional<ChatModel> chatModel;
    private final CustomerGoodsTool goodsTool;
    private final CustomerOrderTool orderTool;
    private final ConcurrentMap<String, List<String>> memory = new ConcurrentHashMap<>();

    public CustomerAgentService(Optional<ChatModel> chatModel, CustomerGoodsTool goodsTool, CustomerOrderTool orderTool) {
        this.chatModel = chatModel;
        this.goodsTool = goodsTool;
        this.orderTool = orderTool;
    }

    public CustomerAgentResponse chat(String message) {
        String normalizedMessage = normalizeMessage(message);
        Optional<String> orderKeyword = orderKeyword(normalizedMessage);
        if (orderKeyword.isPresent()) {
            return new CustomerAgentResponse(orderAnswer(orderKeyword.get()), List.of());
        }

        List<Map<String, Object>> goods = goodsTool.search(normalizedMessage, 6);
        String answer = callModel(null, normalizedMessage, goods)
                .orElse("模型未配置或调用失败，无法生成智能客服回答。请检查 DASHSCOPE_API_KEY。");
        return new CustomerAgentResponse(answer, goods);
    }

    public Flux<CustomerAgentStreamEvent> stream(String sessionId, String message) {
        String currentSessionId = normalizeSessionId(sessionId);
        String normalizedMessage = normalizeMessage(message);
        appendMemory(currentSessionId, "用户：" + normalizedMessage);

        Optional<String> orderKeyword = orderKeyword(normalizedMessage);
        if (orderKeyword.isPresent()) {
            String answer = orderAnswer(orderKeyword.get());
            appendMemory(currentSessionId, "客服：" + answer);
            return Flux.just(CustomerAgentStreamEvent.token(answer), CustomerAgentStreamEvent.done());
        }

        List<Map<String, Object>> goods = goodsTool.search(normalizedMessage, 6);
        if (chatModel.isEmpty()) {
            return Flux.just(
                    CustomerAgentStreamEvent.error("模型未配置：没有创建 DashScope ChatModel。请确认已设置 DASHSCOPE_API_KEY，并重启后端。"),
                    CustomerAgentStreamEvent.done()
            );
        }

        StringBuilder answer = new StringBuilder();
        return Flux.concat(
                streamModel(currentSessionId, normalizedMessage, goods)
                        .doOnNext(answer::append)
                        .map(CustomerAgentStreamEvent::token)
                        .onErrorResume(error -> Flux.just(CustomerAgentStreamEvent.error(
                                "百炼模型调用失败：" + rootMessage(error)
                        ))),
                Flux.defer(() -> {
                    if (!answer.isEmpty()) {
                        appendMemory(currentSessionId, "客服：" + answer);
                        return Flux.just(CustomerAgentStreamEvent.goods(goods), CustomerAgentStreamEvent.done());
                    }
                    return Flux.just(CustomerAgentStreamEvent.done());
                })
        );
    }

    private Optional<String> callModel(String sessionId, String message, List<Map<String, Object>> goods) {
        if (chatModel.isEmpty()) {
            return Optional.empty();
        }
        try {
            String content = ChatClient.create(chatModel.get())
                    .prompt()
                    .system(SYSTEM_PROMPT)
                    .tools(goodsTool, orderTool)
                    .user(prompt(sessionId, message, goods))
                    .call()
                    .content();
            return Optional.ofNullable(content).filter(text -> !text.isBlank());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Flux<String> streamModel(String sessionId, String message, List<Map<String, Object>> goods) {
        return ChatClient.create(chatModel.orElseThrow())
                .prompt()
                .system(SYSTEM_PROMPT)
                .tools(goodsTool, orderTool)
                .user(prompt(sessionId, message, goods))
                .stream()
                .content()
                .filter(token -> token != null && !token.isBlank());
    }

    private String prompt(String sessionId, String message, List<Map<String, Object>> goods) {
        return """
                Recent conversation memory:
                %s

                User need:
                %s

                Text-only RAG product context from database:
                %s
                """.formatted(memoryText(sessionId), message, productContext(goods));
    }

    private String memoryText(String sessionId) {
        if (sessionId == null) {
            return "No previous memory.";
        }
        return String.join("\n", memory.getOrDefault(sessionId, List.of()));
    }

    private String productContext(List<Map<String, Object>> goods) {
        if (goods.isEmpty()) {
            return "No matched products.";
        }
        return goods.stream()
                .map(item -> "- id=%s, name=%s, category=%s, brand=%s, price=%s, desc=%s, sales=%s, inventory=%s"
                        .formatted(item.get("id"), item.get("name"), item.get("categoryName"),
                                item.get("brandName"), item.get("price"), item.get("desc"),
                                item.get("salesCount"), item.get("inventory")))
                .reduce("", (left, right) -> left + right + "\n");
    }

    private void appendMemory(String sessionId, String item) {
        List<String> items = memory.computeIfAbsent(sessionId, key -> new ArrayList<>());
        synchronized (items) {
            items.add(item);
            while (items.size() > MAX_MEMORY_ITEMS) {
                items.remove(0);
            }
        }
    }

    private Optional<String> orderKeyword(String message) {
        String text = message == null ? "" : message.trim();
        if (text.isBlank()) {
            return Optional.empty();
        }

        Matcher orderNoMatcher = ORDER_NO_PATTERN.matcher(text);
        if (orderNoMatcher.find()) {
            return Optional.of(orderNoMatcher.group());
        }

        if (text.matches("\\d+")) {
            return Optional.of(text);
        }

        if (text.contains("订单") || text.toLowerCase().contains("order")) {
            Matcher numberMatcher = NUMBER_PATTERN.matcher(text);
            if (numberMatcher.find()) {
                return Optional.of(numberMatcher.group());
            }
            return Optional.of("");
        }

        return Optional.empty();
    }

    private String orderAnswer(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return "请提供要查询的订单号，例如：查询订单 FR1710000000000。";
        }
        return orderTool.formatOrderAnswer(keyword);
    }

    private String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private String normalizeMessage(String message) {
        return message == null || message.isBlank()
                ? "帮我推荐几件适合现在购买的商品"
                : message.trim();
    }

    private String normalizeSessionId(String sessionId) {
        return sessionId == null || sessionId.isBlank() ? "anonymous" : sessionId.trim();
    }

    public List<Map<String, Object>> allGoods(Integer limit) {
        return goodsTool.listAll(limit);
    }
}
