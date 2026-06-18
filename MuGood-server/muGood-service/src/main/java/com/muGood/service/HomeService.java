package com.muGood.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImages;

@Service
public class HomeService {
    private final JdbcTemplate jdbcTemplate;

    public HomeService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> banners(Integer distributionSite) {
        List<Map<String, Object>> banners = jdbcTemplate.queryForList("""
                select id, img_url imgUrl, href_url hrefUrl, title
                from banner
                where distribution_site = ? and status = 1
                order by sort_order
                """, distributionSite == null ? 1 : distributionSite);
        normalizeImages(banners, "imgUrl");
        return banners;
    }

    public List<Map<String, Object>> headCategories() {
        List<Map<String, Object>> categories = topCategories();
        categories.forEach(category -> {
            Long categoryId = numberValue(category.get("id"));
            category.put("children", children(categoryId));
            category.put("goods", recommendGoods(categoryId, 6));
        });
        return categories;
    }

    public List<Map<String, Object>> newGoods() {
        List<Map<String, Object>> goods = jdbcTemplate.queryForList("""
                select id, name, description `desc`, price, main_picture picture
                from goods
                where is_new = 1 and status = 1
                order by publish_time desc
                limit 8
                """);
        normalizeImages(goods, "picture");
        return goods;
    }

    public List<Map<String, Object>> hotGoods() {
        List<Map<String, Object>> goods = jdbcTemplate.queryForList("""
                select id, name title, description alt, main_picture picture
                from goods
                where is_hot = 1 and status = 1
                order by order_num desc
                limit 8
                """);
        normalizeImages(goods, "picture");
        return goods;
    }

    public List<Map<String, Object>> productPanels() {
        List<Map<String, Object>> categories = topCategories();
        categories.forEach(category -> category.put("goods", recommendGoods(numberValue(category.get("id")), 8)));
        return categories;
    }

    private List<Map<String, Object>> topCategories() {
        List<Map<String, Object>> categories = jdbcTemplate.queryForList("""
                select id, name, picture, sale_info saleInfo
                from category
                where parent_id is null and status = 1
                order by sort_order
                """);
        normalizeImages(categories, "picture");
        return categories;
    }

    private List<Map<String, Object>> children(Long parentId) {
        List<Map<String, Object>> children = jdbcTemplate.queryForList("""
                select id, name, picture
                from category
                where parent_id = ? and status = 1
                order by sort_order
                """, parentId);
        normalizeImages(children, "picture");
        return children;
    }

    private List<Map<String, Object>> recommendGoods(Long categoryId, int limit) {
        List<Map<String, Object>> goods = jdbcTemplate.queryForList("""
                select g.id, g.name, g.description `desc`, g.price, g.main_picture picture
                from goods g
                left join category_goods_recommend r on r.goods_id = g.id and r.category_id = ?
                where g.status = 1
                  and (r.id is not null or g.category_id in (select id from category where parent_id = ?))
                order by coalesce(r.sort_order, 999), g.order_num desc
                limit ?
                """, categoryId, categoryId, limit);
        normalizeImages(goods, "picture");
        return goods;
    }

    private Long numberValue(Object value) {
        return ((Number) value).longValue();
    }
}
