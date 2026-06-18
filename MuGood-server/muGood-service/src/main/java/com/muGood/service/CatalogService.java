package com.muGood.service;

import com.muGood.common.api.PageResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImages;

@Service
public class CatalogService {
    private final JdbcTemplate jdbcTemplate;

    public CatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> category(Long id) {
        Map<String, Object> category = jdbcTemplate.queryForMap("""
                select id, parent_id parentId, name, picture, sale_info saleInfo
                from category
                where id = ?
                """, id);
        normalizeImages(category, "picture");
        List<Map<String, Object>> children = children(id);
        children.forEach(child -> child.put("goods", goodsByCategory(numberValue(child.get("id")), 5)));
        category.put("children", children);
        return category;
    }

    public Map<String, Object> subFilter(Long id) {
        return jdbcTemplate.queryForMap("""
                select c.id, c.name, c.parent_id parentId, p.name parentName
                from category c
                left join category p on p.id = c.parent_id
                where c.id = ?
                """, id);
    }

    public PageResult<Map<String, Object>> goods(Long categoryId, long page, long pageSize, String sortField) {
        String orderBy = switch (sortField == null ? "" : sortField) {
            case "orderNum" -> "order_num desc";
            case "evaluateNum" -> "evaluate_num desc";
            default -> "publish_time desc";
        };
        long offset = Math.max(page - 1, 0) * pageSize;
        long counts = jdbcTemplate.queryForObject("""
                select count(*)
                from goods
                where category_id = ? and status = 1
                """, Long.class, categoryId);
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                select id, name, description `desc`, price, main_picture picture
                from goods
                where category_id = ? and status = 1
                order by %s
                limit ? offset ?
                """.formatted(orderBy), categoryId, pageSize, offset);
        normalizeImages(items, "picture");
        return new PageResult<>(items, counts, page, pageSize);
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

    private List<Map<String, Object>> goodsByCategory(Long categoryId, int limit) {
        List<Map<String, Object>> goods = jdbcTemplate.queryForList("""
                select id, name, description `desc`, price, main_picture picture
                from goods
                where category_id = ? and status = 1
                order by order_num desc
                limit ?
                """, categoryId, limit);
        normalizeImages(goods, "picture");
        return goods;
    }

    private Long numberValue(Object value) {
        return ((Number) value).longValue();
    }
}
