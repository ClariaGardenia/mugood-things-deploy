package com.muGood.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImageList;
import static com.muGood.service.support.ImageUrlSupport.normalizeImages;

@Service
public class GoodsService {
    private final JdbcTemplate jdbcTemplate;

    public GoodsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> goods = jdbcTemplate.queryForMap("""
                select g.id, g.name, g.description `desc`, g.price, g.old_price oldPrice,
                       g.sales_count salesCount, g.comment_count commentCount, g.collect_count collectCount,
                       g.main_picture mainPicture, g.category_id categoryId,
                       b.id brandId, b.name brandName
                from goods g
                left join brand b on b.id = g.brand_id
                where g.id = ?
                """, id);

        Long categoryId = numberValue(goods.get("categoryId"));
        List<Map<String, Object>> categories = jdbcTemplate.queryForList("""
                select c.id, c.name
                from category c
                where c.id = ?
                union all
                select p.id, p.name
                from category c
                join category p on p.id = c.parent_id
                where c.id = ?
                """, categoryId, categoryId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", goods.get("id"));
        result.put("name", goods.get("name"));
        result.put("desc", goods.get("desc"));
        result.put("price", goods.get("price"));
        result.put("oldPrice", goods.get("oldPrice"));
        result.put("salesCount", goods.get("salesCount"));
        result.put("commentCount", goods.get("commentCount"));
        result.put("collectCount", goods.get("collectCount"));
        result.put("mainPictures", pictures(id, 1, goods.get("mainPicture")));
        Map<String, Object> brand = new LinkedHashMap<>();
        brand.put("id", goods.get("brandId"));
        brand.put("name", goods.get("brandName"));
        result.put("brand", brand);
        result.put("categories", categories);
        result.put("details", Map.of(
                "properties", properties(id),
                "pictures", pictures(id, 2, goods.get("mainPicture"))
        ));
        result.put("specs", specs(id));
        result.put("skus", skus(id));
        return result;
    }

    public List<Map<String, Object>> hot(Long id, Integer type, Integer limit) {
        int size = limit == null ? 3 : limit;
        String orderBy = type != null && type == 2 ? "evaluate_num desc" : "order_num desc";
        List<Map<String, Object>> goods = jdbcTemplate.queryForList("""
                select id, name, description `desc`, price, main_picture picture
                from goods
                where id <> ? and status = 1
                order by %s
                limit ?
                """.formatted(orderBy), id, size);
        normalizeImages(goods, "picture");
        return goods;
    }

    public List<Map<String, Object>> relevant(Integer limit) {
        List<Map<String, Object>> goods = jdbcTemplate.queryForList("""
                select id, name, description `desc`, price, main_picture picture
                from goods
                where status = 1
                order by order_num desc
                limit ?
                """, limit == null ? 4 : limit);
        normalizeImages(goods, "picture");
        return goods;
    }

    private List<String> pictures(Long goodsId, int type, Object fallback) {
        List<String> pictures = jdbcTemplate.queryForList("""
                select picture_url
                from goods_picture
                where goods_id = ? and picture_type = ?
                order by sort_order
                """, String.class, goodsId, type);
        if (pictures.isEmpty() && fallback != null) {
            pictures = List.of(String.valueOf(fallback));
        }
        return normalizeImageList(pictures);
    }

    private List<Map<String, Object>> properties(Long goodsId) {
        return jdbcTemplate.queryForList("""
                select name, value
                from goods_property
                where goods_id = ?
                order by sort_order
                """, goodsId);
    }

    private List<Map<String, Object>> specs(Long goodsId) {
        List<Map<String, Object>> specs = jdbcTemplate.queryForList("""
                select id, name
                from spec
                where goods_id = ?
                order by sort_order
                """, goodsId);
        specs.forEach(spec -> spec.put("values", jdbcTemplate.queryForList("""
                select id, name, picture
                from spec_value
                where spec_id = ?
                order by sort_order
                """, spec.get("id"))));
        specs.forEach(spec -> normalizeImages((List<Map<String, Object>>) spec.get("values"), "picture"));
        return specs;
    }

    private List<Map<String, Object>> skus(Long goodsId) {
        List<Map<String, Object>> skus = jdbcTemplate.queryForList("""
                select id, price, old_price oldPrice, inventory, picture
                from sku
                where goods_id = ? and status = 1
                """, goodsId);
        skus.forEach(sku -> sku.put("specs", jdbcTemplate.queryForList("""
                select spec_name name, value_name valueName
                from sku_spec_value
                where sku_id = ?
                order by spec_id
                """, sku.get("id"))));
        normalizeImages(skus, "picture");
        return new ArrayList<>(skus);
    }

    private Long numberValue(Object value) {
        return ((Number) value).longValue();
    }
}
