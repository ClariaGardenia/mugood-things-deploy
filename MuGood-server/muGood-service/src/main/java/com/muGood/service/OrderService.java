package com.muGood.service;

import com.muGood.common.api.PageResult;
import com.muGood.service.dto.CreateOrderRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImages;

@Service
public class OrderService {
    private static final long DEMO_USER_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    public OrderService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> preOrder() {
        List<Map<String, Object>> addresses = jdbcTemplate.queryForList("""
                select id, receiver, contact, full_location as "fullLocation", address, is_default as "isDefault"
                from user_address
                where user_id = ?
                order by is_default, id
                """, DEMO_USER_ID);
        List<Map<String, Object>> goods = selectedCartGoods();
        Map<String, Object> summary = summary(goods);
        return Map.of("userAddresses", addresses, "goods", goods, "summary", summary);
    }

    @Transactional
    public Map<String, Object> create(CreateOrderRequest request) {
        Map<String, Object> address = jdbcTemplate.queryForMap("""
                select id, receiver, contact, full_location as "fullLocation", address
                from user_address
                where id = ? and user_id = ?
                """, request.addressId(), DEMO_USER_ID);
        List<Map<String, Object>> goods = selectedCartGoods();
        Map<String, Object> summary = summary(goods);
        String orderNo = "FR" + System.currentTimeMillis();
        BigDecimal totalPrice = decimal(summary.get("totalPrice"));
        BigDecimal postFee = decimal(summary.get("postFee"));
        BigDecimal totalPayPrice = decimal(summary.get("totalPayPrice"));

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into orders(order_no, user_id, address_id, receiver, contact, full_location, address,
                                       goods_count, total_price, post_fee, total_pay_price, pay_money,
                                       delivery_time_type, pay_type, pay_channel, buyer_message, order_state, countdown, pay_deadline)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, 1800, current_timestamp + interval '30 minutes')
                    """, new String[]{"id"});
            statement.setString(1, orderNo);
            statement.setLong(2, DEMO_USER_ID);
            statement.setLong(3, request.addressId());
            statement.setString(4, String.valueOf(address.get("receiver")));
            statement.setString(5, String.valueOf(address.get("contact")));
            statement.setString(6, String.valueOf(address.get("fullLocation")));
            statement.setString(7, String.valueOf(address.get("address")));
            statement.setInt(8, ((Number) summary.get("goodsCount")).intValue());
            statement.setBigDecimal(9, totalPrice);
            statement.setBigDecimal(10, postFee);
            statement.setBigDecimal(11, totalPayPrice);
            statement.setBigDecimal(12, totalPayPrice);
            statement.setInt(13, valueOrDefault(request.deliveryTimeType(), 1));
            statement.setInt(14, valueOrDefault(request.payType(), 1));
            statement.setInt(15, valueOrDefault(request.payChannel(), 1));
            statement.setString(16, request.buyerMessage());
            return statement;
        }, keyHolder);
        Long orderId = keyHolder.getKey().longValue();

        for (Map<String, Object> item : goods) {
            jdbcTemplate.update("""
                    insert into order_sku(order_id, goods_id, sku_id, name, image, attrs_text, price, real_pay,
                                          quantity, total_price, total_pay_price)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, orderId, item.get("goodsId"), item.get("skuId"), item.get("name"), item.get("picture"),
                    item.get("attrsText"), item.get("price"), item.get("price"), item.get("count"),
                    item.get("totalPrice"), item.get("totalPayPrice"));
        }
        jdbcTemplate.update("delete from cart_item where user_id = ? and selected = 1", DEMO_USER_ID);
        return Map.of("id", orderId);
    }

    public PageResult<Map<String, Object>> list(Integer orderState, long page, long pageSize) {
        long offset = Math.max(page - 1, 0) * pageSize;
        boolean all = orderState == null || orderState == 0;
        Long counts = jdbcTemplate.queryForObject("""
                select count(*)
                from orders
                where user_id = ? and (? = true or order_state = ?)
                """, Long.class, DEMO_USER_ID, all, orderState);
        List<Map<String, Object>> orders = jdbcTemplate.queryForList("""
                select id, order_no as "orderNo", to_char(created_at, 'YYYY-MM-DD HH24:MI:SS') as "createTime",
                       order_state as "orderState", countdown, pay_money as "payMoney", post_fee as "postFee"
                from orders
                where user_id = ? and (? = true or order_state = ?)
                order by created_at desc
                limit ? offset ?
                """, DEMO_USER_ID, all, orderState, pageSize, offset);
        orders.forEach(order -> order.put("skus", orderSkus(((Number) order.get("id")).longValue())));
        return new PageResult<>(orders, counts == null ? 0 : counts, page, pageSize);
    }

    public Map<String, Object> detail(Long id) {
        return jdbcTemplate.queryForMap("""
                select id, pay_money as "payMoney",
                       greatest(0, floor(extract(epoch from (coalesce(pay_deadline, current_timestamp + countdown * interval '1 second') - current_timestamp)))::int) countdown
                from orders
                where id = ? and user_id = ?
                """, id, DEMO_USER_ID);
    }

    private List<Map<String, Object>> selectedCartGoods() {
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                select c.id, c.goods_id as "goodsId", c.sku_id as "skuId", g.name, coalesce(s.picture, g.main_picture) picture,
                       s.price, c.count, s.price * c.count as "totalPrice", s.price * c.count as "totalPayPrice",
                       coalesce(string_agg(ssv.spec_name || ': ' || ssv.value_name, ' ' order by ssv.spec_id), '') as "attrsText"
                from cart_item c
                join goods g on g.id = c.goods_id
                join sku s on s.id = c.sku_id
                left join sku_spec_value ssv on ssv.sku_id = s.id
                where c.user_id = ? and c.selected = 1
                group by c.id, c.goods_id, c.sku_id, g.name, s.picture, g.main_picture, s.price, c.count
                """, DEMO_USER_ID);
        normalizeImages(items, "picture");
        return items;
    }

    private List<Map<String, Object>> orderSkus(Long orderId) {
        List<Map<String, Object>> skus = jdbcTemplate.queryForList("""
                select id, goods_id as "goodsId", sku_id as "skuId", name, image, attrs_text as "attrsText", real_pay as "realPay", quantity
                from order_sku
                where order_id = ?
                order by id
                """, orderId);
        normalizeImages(skus, "image");
        return skus;
    }

    private Map<String, Object> summary(List<Map<String, Object>> goods) {
        int goodsCount = goods.stream().mapToInt(item -> ((Number) item.get("count")).intValue()).sum();
        BigDecimal totalPrice = goods.stream()
                .map(item -> decimal(item.get("totalPrice")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal postFee = totalPrice.compareTo(BigDecimal.ZERO) > 0 && totalPrice.compareTo(new BigDecimal("99")) < 0
                ? new BigDecimal("10.00")
                : BigDecimal.ZERO;
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("goodsCount", goodsCount);
        summary.put("totalPrice", totalPrice);
        summary.put("postFee", postFee);
        summary.put("totalPayPrice", totalPrice.add(postFee));
        return summary;
    }

    private BigDecimal decimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private int valueOrDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
