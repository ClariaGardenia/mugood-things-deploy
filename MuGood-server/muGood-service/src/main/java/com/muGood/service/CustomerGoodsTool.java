package com.muGood.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImages;

@Service
public class CustomerGoodsTool {
    private final JdbcTemplate jdbcTemplate;

    public CustomerGoodsTool(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool(name = "searchGoods", description = "Search products in the mall database by customer need, category, gender, style, season, or keyword.")
    public List<Map<String, Object>> searchGoods(
            @ToolParam(description = "Customer need or keyword, for example: men's short sleeve, fruit, blueberry, storage box") String query,
            @ToolParam(description = "Maximum number of products to return") Integer limit) {
        return search(query, limit).stream()
                .map(this::aiSafeGoods)
                .toList();
    }

    public List<Map<String, Object>> search(String query, Integer limit) {
        int size = limit == null || limit <= 0 ? 6 : Math.min(limit, 12);
        List<Map<String, Object>> goods = loadActiveGoods(300);
        List<String> terms = expandTerms(query);
        if (terms.isEmpty()) {
            return List.of();
        }

        return goods.stream()
                .map(item -> Map.entry(item, score(item, terms)))
                .filter(entry -> entry.getValue() > 0)
                .sorted(Map.Entry.<Map<String, Object>, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> numberValue(entry.getKey().get("salesCount")), Comparator.reverseOrder()))
                .limit(size)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Map<String, Object>> listAll(Integer limit) {
        int size = limit == null || limit <= 0 ? 50 : Math.min(limit, 200);
        return loadActiveGoods(size);
    }

    private List<Map<String, Object>> loadActiveGoods(int limit) {
        List<Map<String, Object>> goods = jdbcTemplate.queryForList("""
                select g.id, g.name, g.description `desc`, g.price, g.old_price oldPrice,
                       g.main_picture picture, g.sales_count salesCount, g.inventory,
                       c.name categoryName, b.name brandName
                from goods g
                left join category c on c.id = g.category_id
                left join brand b on b.id = g.brand_id
                where g.status = 1
                order by g.is_hot desc, g.sales_count desc, g.updated_at desc, g.id desc
                limit ?
                """, limit);
        normalizeImages(goods, "picture");
        goods.forEach(item -> item.put("detailPath", "/detail/" + item.get("id")));
        return goods;
    }

    private List<String> expandTerms(String query) {
        String text = query == null ? "" : query.toLowerCase(Locale.ROOT);
        List<String> terms = new ArrayList<>();
        addIfPresent(text, terms, List.of("短袖", "t恤", "t-shirt", "tee", "半袖"), List.of("短袖", "t恤", "T恤", "半袖", "上衣"));
        addIfPresent(text, terms, List.of("男", "男生", "男士", "男性"), List.of("男", "男士", "男款"));
        addIfPresent(text, terms, List.of("女", "女生", "女士", "女性"), List.of("女", "女士", "女款"));
        addIfPresent(text, terms, List.of("水果", "鲜果", "果", "吃点水果", "蓝莓", "莓"), List.of("水果", "鲜果", "果", "蓝莓", "莓"));
        addIfPresent(text, terms, List.of("收纳", "箱", "盒"), List.of("收纳", "箱", "盒"));
        addIfPresent(text, terms, List.of("零食", "吃点零食"), List.of("零食", "食品", "小吃"));
        addIfPresent(text, terms, List.of("衬衫"), List.of("衬衫"));
        addIfPresent(text, terms, List.of("裤", "裤子"), List.of("裤", "长裤", "短裤"));
        addIfPresent(text, terms, List.of("鞋"), List.of("鞋", "运动鞋", "休闲鞋"));
        addIfPresent(text, terms, List.of("夏", "夏天", "夏季", "清爽"), List.of("夏", "薄", "透气", "清爽"));
        addIfPresent(text, terms, List.of("通勤", "上班"), List.of("通勤", "商务", "简约"));
        addIfPresent(text, terms, List.of("运动", "健身"), List.of("运动", "速干", "透气"));

        if (terms.isEmpty()) {
            for (String token : text.split("[\\s,，。！？、]+")) {
                if (token.length() >= 2) {
                    terms.add(token);
                }
            }
        }
        return terms.stream().distinct().toList();
    }

    private void addIfPresent(String text, List<String> terms, List<String> triggers, List<String> expandedTerms) {
        if (triggers.stream().anyMatch(text::contains)) {
            terms.addAll(expandedTerms);
        }
    }

    private int score(Map<String, Object> item, List<String> terms) {
        String name = stringValue(item.get("name")).toLowerCase(Locale.ROOT);
        String desc = stringValue(item.get("desc")).toLowerCase(Locale.ROOT);
        String category = stringValue(item.get("categoryName")).toLowerCase(Locale.ROOT);
        String brand = stringValue(item.get("brandName")).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : terms) {
            String keyword = term.toLowerCase(Locale.ROOT);
            if (name.contains(keyword)) score += 8;
            if (category.contains(keyword)) score += 6;
            if (desc.contains(keyword)) score += 3;
            if (brand.contains(keyword)) score += 1;
        }
        return score;
    }

    private Integer numberValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return 0;
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Map<String, Object> aiSafeGoods(Map<String, Object> item) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", item.get("id"));
        result.put("name", item.get("name"));
        result.put("desc", item.get("desc"));
        result.put("price", item.get("price"));
        result.put("salesCount", item.get("salesCount"));
        result.put("inventory", item.get("inventory"));
        result.put("categoryName", item.get("categoryName"));
        result.put("brandName", item.get("brandName"));
        return result;
    }
}
