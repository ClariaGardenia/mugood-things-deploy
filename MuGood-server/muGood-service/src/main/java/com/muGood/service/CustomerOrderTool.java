package com.muGood.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.muGood.service.support.ImageUrlSupport.normalizeImages;

@Service
public class CustomerOrderTool {
    private static final long DEMO_USER_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    public CustomerOrderTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(name = "queryOrderByOrderNo", description = "Query a customer's order from the mall database by order number or numeric order id.")
    public Map<String, Object> queryOrderByOrderNo(
            @ToolParam(description = "Order number or numeric order id, for example FR1710000000000 or 8") String orderNo) {
        return findByKeyword(orderNo).orElseGet(() -> Map.of(
                "found", false,
                "message", "没有查询到对应订单，请确认订单号是否正确。"
        ));
    }

    public Optional<Map<String, Object>> findByKeyword(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isBlank()) {
            return Optional.empty();
        }

        Long numericId = parseNumericId(normalizedKeyword);
        List<Map<String, Object>> orders = jdbcTemplate.queryForList("""
                select id, order_no as "orderNo", receiver, contact, full_location as "fullLocation", address,
                       goods_count as "goodsCount", total_price as "totalPrice", post_fee as "postFee",
                       total_pay_price as "totalPayPrice", pay_money as "payMoney", order_state as "orderState",
                       to_char(created_at, 'YYYY-MM-DD HH24:MI:SS') as "createTime",
                       to_char(pay_deadline, 'YYYY-MM-DD HH24:MI:SS') as "payDeadline",
                       greatest(0, floor(extract(epoch from (coalesce(pay_deadline, current_timestamp + countdown * interval '1 second') - current_timestamp)))::int) countdown
                from orders
                where user_id = ? and (order_no = ? or (? is not null and id = ?))
                limit 1
                """, DEMO_USER_ID, normalizedKeyword, numericId, numericId);
        if (orders.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> order = new LinkedHashMap<>(orders.get(0));
        order.put("found", true);
        order.put("orderStateText", orderStateText(order.get("orderState")));
        order.put("skus", orderSkus(((Number) order.get("id")).longValue()));
        return Optional.of(order);
    }

    public String formatOrderAnswer(String keyword) {
        return findByKeyword(keyword)
                .map(this::formatFoundOrder)
                .orElse("没有查询到订单「%s」。请确认订单号是否正确，或者在“我的订单”里复制完整订单号后再试。".formatted(normalizeKeyword(keyword)));
    }

    private List<Map<String, Object>> orderSkus(Long orderId) {
        List<Map<String, Object>> skus = jdbcTemplate.queryForList("""
                select id, goods_id as "goodsId", sku_id as "skuId", name, image, attrs_text as "attrsText",
                       real_pay as "realPay", quantity, total_pay_price as "totalPayPrice"
                from order_sku
                where order_id = ?
                order by id
                """, orderId);
        normalizeImages(skus, "image");
        return skus;
    }

    private String formatFoundOrder(Map<String, Object> order) {
        StringBuilder builder = new StringBuilder();
        builder.append("查询到您的订单：\n");
        builder.append("订单号：").append(order.get("orderNo")).append("\n");
        builder.append("订单状态：").append(order.get("orderStateText")).append("\n");
        builder.append("下单时间：").append(order.get("createTime")).append("\n");
        builder.append("实付金额：￥").append(order.get("payMoney")).append("\n");
        builder.append("收货人：").append(order.get("receiver")).append("，").append(order.get("contact")).append("\n");
        builder.append("收货地址：").append(order.get("fullLocation")).append(order.get("address")).append("\n");

        Object skusObject = order.get("skus");
        if (skusObject instanceof List<?> skus && !skus.isEmpty()) {
            builder.append("订单商品：\n");
            for (Object skuObject : skus) {
                if (skuObject instanceof Map<?, ?> sku) {
                    builder.append("- ")
                            .append(sku.get("name"))
                            .append(" × ")
                            .append(sku.get("quantity"))
                            .append("，￥")
                            .append(sku.get("totalPayPrice"))
                            .append("\n");
                }
            }
        }
        return builder.toString().trim();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim();
    }

    private Long parseNumericId(String keyword) {
        return keyword.matches("\\d+") ? Long.parseLong(keyword) : null;
    }

    private String orderStateText(Object value) {
        int state = value instanceof Number number ? number.intValue() : 0;
        return switch (state) {
            case 1 -> "待付款";
            case 2 -> "待发货";
            case 3 -> "待收货";
            case 4 -> "待评价";
            case 5 -> "已完成";
            case 6 -> "已取消";
            default -> "未知状态";
        };
    }
}
