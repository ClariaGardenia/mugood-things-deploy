package com.muGood.service;

import com.muGood.service.dto.AddCartRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImages;

@Service
public class CartService {
    private static final long DEMO_USER_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    public CartService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list() {
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                select c.id, c.goods_id as "goodsId", c.sku_id as "skuId", g.name, coalesce(s.picture, g.main_picture) picture,
                       s.price, c.count, c.selected,
                       coalesce(string_agg(ssv.spec_name || ': ' || ssv.value_name, ' ' order by ssv.spec_id), '') as "attrsText"
                from cart_item c
                join goods g on g.id = c.goods_id
                join sku s on s.id = c.sku_id
                left join sku_spec_value ssv on ssv.sku_id = s.id
                where c.user_id = ?
                group by c.id, c.goods_id, c.sku_id, g.name, s.picture, g.main_picture, s.price, c.count, c.selected
                order by c.updated_at desc, c.id desc
                """, DEMO_USER_ID);
        normalizeImages(items, "picture");
        return items;
    }

    @Transactional
    public void add(AddCartRequest request) {
        Map<String, Object> sku = jdbcTemplate.queryForMap("""
                select id, goods_id as "goodsId"
                from sku
                where id = ?
                """, request.skuId());
        jdbcTemplate.update("""
                insert into cart_item(user_id, goods_id, sku_id, count, selected)
                values (?, ?, ?, ?, 1)
                on conflict (user_id, sku_id)
                do update set count = cart_item.count + excluded.count,
                              selected = 1,
                              updated_at = current_timestamp
                """, DEMO_USER_ID, sku.get("goodsId"), request.skuId(), request.count());
    }

    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        ids.forEach(id -> jdbcTemplate.update("""
                delete from cart_item
                where user_id = ? and sku_id = ?
                """, DEMO_USER_ID, id));
    }

    @Transactional
    public void merge(List<Map<String, Object>> items) {
        if (items == null) {
            return;
        }
        for (Map<String, Object> item : items) {
            Object skuId = item.get("skuId");
            Object count = item.get("count");
            if (skuId != null && count != null) {
                add(new AddCartRequest(((Number) skuId).longValue(), ((Number) count).intValue()));
            }
        }
    }
}
