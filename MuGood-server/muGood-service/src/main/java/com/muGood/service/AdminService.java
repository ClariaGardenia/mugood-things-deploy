package com.muGood.service;

import com.muGood.common.api.PageResult;
import com.muGood.common.exception.BizException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImages;
import static com.muGood.service.support.PasswordHashSupport.hashPassword;

@Service
public class AdminService {
    private final JdbcTemplate jdbcTemplate;

    public AdminService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> summary() {
        Long goodsCount = jdbcTemplate.queryForObject("select count(*) from goods", Long.class);
        Long categoryCount = jdbcTemplate.queryForObject("select count(*) from category", Long.class);
        Long orderCount = jdbcTemplate.queryForObject("select count(*) from orders", Long.class);
        Long userCount = jdbcTemplate.queryForObject("select count(*) from \"user\"", Long.class);
        Object salesAmount = jdbcTemplate.queryForObject("select coalesce(sum(pay_money), 0) from orders where order_state <> 6", Object.class);
        return Map.of(
                "goodsCount", goodsCount == null ? 0 : goodsCount,
                "categoryCount", categoryCount == null ? 0 : categoryCount,
                "orderCount", orderCount == null ? 0 : orderCount,
                "userCount", userCount == null ? 0 : userCount,
                "salesAmount", salesAmount == null ? 0 : salesAmount
        );
    }

    public PageResult<Map<String, Object>> goods(long page, long pageSize, String keyword) {
        String likeKeyword = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        long offset = Math.max(page - 1, 0) * pageSize;
        Long counts = jdbcTemplate.queryForObject("""
                select count(*)
                from goods g
                where (? = '%%' or g.name like ?)
                """, Long.class, likeKeyword, likeKeyword);
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                select g.id, g.name, g.description as "desc", g.price, g.old_price as "oldPrice", g.main_picture picture,
                       g.inventory, g.sales_count as "salesCount", g.status, g.category_id as "categoryId", g.brand_id as "brandId",
                       g.is_new as "isNew", g.is_hot as "isHot", c.name as "categoryName", b.name as "brandName"
                from goods g
                left join category c on c.id = g.category_id
                left join brand b on b.id = g.brand_id
                where (? = '%%' or g.name like ?)
                order by g.updated_at desc, g.id desc
                limit ? offset ?
                """, likeKeyword, likeKeyword, pageSize, offset);
        normalizeImages(items, "picture");
        return new PageResult<>(items, counts == null ? 0 : counts, page, pageSize);
    }

    public Map<String, Object> goodsDetail(Long id) {
        Map<String, Object> goods = jdbcTemplate.queryForMap("""
                select g.id, g.name, g.description as "desc", g.price, g.old_price as "oldPrice", g.main_picture picture,
                       g.inventory, g.sales_count as "salesCount", g.status, g.category_id as "categoryId", g.brand_id as "brandId",
                       g.is_new as "isNew", g.is_hot as "isHot", c.name as "categoryName", b.name as "brandName"
                from goods g
                left join category c on c.id = g.category_id
                left join brand b on b.id = g.brand_id
                where g.id = ?
                """, id);
        normalizeImages(goods, "picture");

        List<Map<String, Object>> mainPictures = goodsPictures(id, 1);
        List<Map<String, Object>> detailPictures = goodsPictures(id, 2);
        List<Map<String, Object>> properties = jdbcTemplate.queryForList("""
                select id, name, value, sort_order as "sortOrder"
                from goods_property
                where goods_id = ?
                order by sort_order
                """, id);
        List<Map<String, Object>> specs = specs(id);
        List<Map<String, Object>> skus = adminSkus(id);

        Map<String, Object> result = new LinkedHashMap<>(goods);
        result.put("mainPictures", mainPictures);
        result.put("detailPictures", detailPictures);
        result.put("properties", properties);
        result.put("specs", specs);
        result.put("skus", skus);
        return result;
    }

    public Map<String, Object> options() {
        List<Map<String, Object>> leafCategories = jdbcTemplate.queryForList("""
                select id, name
                from category
                where level = 2 and status = 1
                order by parent_id, sort_order
                """);
        List<Map<String, Object>> parentCategories = jdbcTemplate.queryForList("""
                select id, name
                from category
                where parent_id is null
                order by sort_order
                """);
        List<Map<String, Object>> brands = jdbcTemplate.queryForList("""
                select id, name
                from brand
                where status = 1
                order by sort_order
                """);
        return Map.of("leafCategories", leafCategories, "parentCategories", parentCategories, "brands", brands);
    }

    @Transactional
    public Map<String, Object> createGoods(Map<String, Object> body) {
        fillPrimaryPicture(body);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into goods(category_id, brand_id, name, description, price, old_price, main_picture,
                                      inventory, is_new, is_hot, status)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            fillGoodsStatement(statement, body);
            return statement;
        }, keyHolder);
        Long goodsId = keyHolder.getKey().longValue();
        replaceGoodsDetail(goodsId, body);
        return Map.of("id", goodsId);
    }

    @Transactional
    public void updateGoods(Long id, Map<String, Object> body) {
        fillPrimaryPicture(body);
        jdbcTemplate.update("""
                update goods
                set category_id = ?, brand_id = ?, name = ?, description = ?, price = ?, old_price = ?,
                    main_picture = ?, inventory = ?, is_new = ?, is_hot = ?, status = ?, updated_at = current_timestamp
                where id = ?
                """,
                longValue(body.get("categoryId")),
                longValue(body.get("brandId")),
                stringValue(body.get("name")),
                stringValue(body.get("desc")),
                decimalValue(body.get("price")),
                decimalValue(body.get("oldPrice")),
                stringValue(body.get("picture")),
                intValue(body.get("inventory"), 0),
                intValue(body.get("isNew"), 0),
                intValue(body.get("isHot"), 0),
                intValue(body.get("status"), 1),
                id);
        replaceGoodsDetail(id, body);
    }

    @Transactional
    public void deleteGoods(Long id) {
        jdbcTemplate.update("update goods set status = 0, updated_at = current_timestamp where id = ?", id);
    }

    public List<Map<String, Object>> categories() {
        List<Map<String, Object>> categories = jdbcTemplate.queryForList("""
                select c.id, c.parent_id as "parentId", c.name, c.picture, c.level, c.sort_order as "sortOrder", c.status,
                       p.name as "parentName"
                from category c
                left join category p on p.id = c.parent_id
                order by c.level, coalesce(c.parent_id, c.id), c.sort_order
                """);
        normalizeImages(categories, "picture");
        return categories;
    }

    @Transactional
    public Map<String, Object> createCategory(Map<String, Object> body) {
        Long parentId = nullableLong(body.get("parentId"));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into category(parent_id, name, picture, sale_info, level, sort_order, status)
                    values (?, ?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            if (parentId == null) {
                statement.setObject(1, null);
                statement.setInt(5, 1);
            } else {
                statement.setLong(1, parentId);
                statement.setInt(5, 2);
            }
            statement.setString(2, stringValue(body.get("name")));
            statement.setString(3, stringValue(body.get("picture")));
            statement.setString(4, stringValue(body.get("saleInfo")));
            statement.setInt(6, intValue(body.get("sortOrder"), 0));
            statement.setInt(7, intValue(body.get("status"), 1));
            return statement;
        }, keyHolder);
        return Map.of("id", keyHolder.getKey().longValue());
    }

    @Transactional
    public void updateCategory(Long id, Map<String, Object> body) {
        Long parentId = nullableLong(body.get("parentId"));
        jdbcTemplate.update("""
                update category
                set parent_id = ?, name = ?, picture = ?, sale_info = ?, level = ?, sort_order = ?,
                    status = ?, updated_at = current_timestamp
                where id = ?
                """, parentId, stringValue(body.get("name")), stringValue(body.get("picture")),
                stringValue(body.get("saleInfo")), parentId == null ? 1 : 2,
                intValue(body.get("sortOrder"), 0), intValue(body.get("status"), 1), id);
    }

    @Transactional
    public void deleteCategory(Long id) {
        jdbcTemplate.update("update category set status = 0, updated_at = current_timestamp where id = ?", id);
    }

    public PageResult<Map<String, Object>> orders(long page, long pageSize, Integer orderState) {
        long offset = Math.max(page - 1, 0) * pageSize;
        boolean all = orderState == null || orderState == 0;
        Long counts = jdbcTemplate.queryForObject("""
                select count(*)
                from orders
                where (? = true or order_state = ?)
                """, Long.class, all, orderState);
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                select id, order_no as "orderNo", receiver, contact, goods_count as "goodsCount", pay_money as "payMoney",
                       order_state as "orderState", to_char(created_at, 'YYYY-MM-DD HH24:MI:SS') as "createTime"
                from orders
                where (? = true or order_state = ?)
                order by created_at desc
                limit ? offset ?
                """, all, orderState, pageSize, offset);
        return new PageResult<>(items, counts == null ? 0 : counts, page, pageSize);
    }

    @Transactional
    public void updateOrderState(Long id, Integer orderState) {
        jdbcTemplate.update("""
                update orders
                set order_state = ?, updated_at = current_timestamp,
                    shipped_at = case when ? = 3 then current_timestamp else shipped_at end,
                    received_at = case when ? = 5 then current_timestamp else received_at end,
                    canceled_at = case when ? = 6 then current_timestamp else canceled_at end
                where id = ?
                """, orderState, orderState, orderState, orderState, id);
    }

    public List<Map<String, Object>> banners() {
        List<Map<String, Object>> banners = jdbcTemplate.queryForList("""
                select id, img_url as "imgUrl", href_url as "hrefUrl", distribution_site as "distributionSite",
                       title, sort_order as "sortOrder", status
                from banner
                order by distribution_site, sort_order
                """);
        normalizeImages(banners, "imgUrl");
        return banners;
    }

    @Transactional
    public Map<String, Object> createBanner(Map<String, Object> body) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    insert into banner(img_url, href_url, distribution_site, title, sort_order, status)
                    values (?, ?, ?, ?, ?, ?)
                    """, new String[]{"id"});
            statement.setString(1, stringValue(body.get("imgUrl")));
            statement.setString(2, stringValue(body.get("hrefUrl")));
            statement.setInt(3, intValue(body.get("distributionSite"), 1));
            statement.setString(4, stringValue(body.get("title")));
            statement.setInt(5, intValue(body.get("sortOrder"), 0));
            statement.setInt(6, intValue(body.get("status"), 1));
            return statement;
        }, keyHolder);
        return Map.of("id", keyHolder.getKey().longValue());
    }

    @Transactional
    public void updateBanner(Long id, Map<String, Object> body) {
        jdbcTemplate.update("""
                update banner
                set img_url = ?, href_url = ?, distribution_site = ?, title = ?, sort_order = ?,
                    status = ?, updated_at = current_timestamp
                where id = ?
                """, stringValue(body.get("imgUrl")), stringValue(body.get("hrefUrl")),
                intValue(body.get("distributionSite"), 1), stringValue(body.get("title")),
                intValue(body.get("sortOrder"), 0), intValue(body.get("status"), 1), id);
    }

    @Transactional
    public void deleteBanner(Long id) {
        jdbcTemplate.update("update banner set status = 0, updated_at = current_timestamp where id = ?", id);
    }

    public PageResult<Map<String, Object>> users(long page, long pageSize, String keyword) {
        String likeKeyword = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        long offset = Math.max(page - 1, 0) * pageSize;
        Long counts = jdbcTemplate.queryForObject("""
                select count(*)
                from "user"
                where (? = '%%' or account like ? or nickname like ? or mobile like ?)
                """, Long.class, likeKeyword, likeKeyword, likeKeyword, likeKeyword);
        List<Map<String, Object>> items = jdbcTemplate.queryForList("""
                select id, account, nickname, mobile, email, gender, status, to_char(created_at, 'YYYY-MM-DD HH24:MI:SS') as "createTime"
                from "user"
                where (? = '%%' or account like ? or nickname like ? or mobile like ?)
                order by created_at desc
                limit ? offset ?
                """, likeKeyword, likeKeyword, likeKeyword, likeKeyword, pageSize, offset);
        return new PageResult<>(items, counts == null ? 0 : counts, page, pageSize);
    }

    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        jdbcTemplate.update("update \"user\" set status = ?, updated_at = current_timestamp where id = ?", status, id);
    }

    @Transactional
    public void updateUserPassword(Long id, String password) {
        if (password == null || password.length() < 6 || password.length() > 32) {
            throw new BizException("USER_001", "密码长度需为 6 到 32 位");
        }
        jdbcTemplate.update("""
                update "user"
                set password_hash = ?, updated_at = current_timestamp
                where id = ?
                """, hashPassword(password), id);
    }

    @Transactional
    public void updateGoodsStatus(Long id, Integer status) {
        jdbcTemplate.update("update goods set status = ?, updated_at = current_timestamp where id = ?", status, id);
    }

    private void fillGoodsStatement(PreparedStatement statement, Map<String, Object> body) throws java.sql.SQLException {
        statement.setLong(1, longValue(body.get("categoryId")));
        statement.setLong(2, longValue(body.get("brandId")));
        statement.setString(3, stringValue(body.get("name")));
        statement.setString(4, stringValue(body.get("desc")));
        statement.setBigDecimal(5, decimalValue(body.get("price")));
        statement.setBigDecimal(6, decimalValue(body.get("oldPrice")));
        statement.setString(7, stringValue(body.get("picture")));
        statement.setInt(8, intValue(body.get("inventory"), 0));
        statement.setInt(9, intValue(body.get("isNew"), 0));
        statement.setInt(10, intValue(body.get("isHot"), 0));
        statement.setInt(11, intValue(body.get("status"), 1));
    }

    private void createDefaultSku(Long goodsId, Map<String, Object> body) {
        jdbcTemplate.update("""
                insert into sku(goods_id, sku_code, price, old_price, inventory, picture, status)
                values (?, ?, ?, ?, ?, ?, ?)
                """, goodsId, "GOODS-" + goodsId + "-DEFAULT", decimalValue(body.get("price")),
                decimalValue(body.get("oldPrice")), intValue(body.get("inventory"), 0),
                stringValue(body.get("picture")), intValue(body.get("status"), 1));
    }

    private List<Map<String, Object>> goodsPictures(Long goodsId, int type) {
        List<Map<String, Object>> pictures = jdbcTemplate.queryForList("""
                select id, picture_url url, sort_order as "sortOrder"
                from goods_picture
                where goods_id = ? and picture_type = ?
                order by sort_order
                """, goodsId, type);
        normalizeImages(pictures, "url");
        return pictures;
    }

    private List<Map<String, Object>> specs(Long goodsId) {
        List<Map<String, Object>> specs = jdbcTemplate.queryForList("""
                select id, name, sort_order as "sortOrder"
                from spec
                where goods_id = ?
                order by sort_order
                """, goodsId);
        specs.forEach(spec -> {
            List<Map<String, Object>> values = jdbcTemplate.queryForList("""
                    select id, name, picture, sort_order as "sortOrder"
                    from spec_value
                    where spec_id = ?
                    order by sort_order
                    """, spec.get("id"));
            normalizeImages(values, "picture");
            spec.put("values", values);
        });
        return specs;
    }

    private List<Map<String, Object>> adminSkus(Long goodsId) {
        List<Map<String, Object>> skus = jdbcTemplate.queryForList("""
                select id, sku_code as "skuCode", price, old_price as "oldPrice", inventory, picture, status
                from sku
                where goods_id = ? and (sku_code is null or sku_code not like 'ARCHIVED-%')
                order by id
                """, goodsId);
        skus.forEach(sku -> sku.put("specs", jdbcTemplate.queryForList("""
                select spec_name name, value_name as "valueName"
                from sku_spec_value
                where sku_id = ?
                order by spec_id
                """, sku.get("id"))));
        normalizeImages(skus, "picture");
        return skus;
    }

    private void replaceGoodsDetail(Long goodsId, Map<String, Object> body) {
        deleteGoodsDetail(goodsId);
        saveGoodsPictures(goodsId, listValue(body.get("mainPictures")), 1);
        saveGoodsPictures(goodsId, listValue(body.get("detailPictures")), 2);
        saveProperties(goodsId, listValue(body.get("properties")));
        Map<String, Map<String, Long>> specValueIds = saveSpecs(goodsId, listValue(body.get("specs")));
        List<Map<String, Object>> skus = listValue(body.get("skus"));
        if (skus.isEmpty()) {
            createDefaultSku(goodsId, body);
        } else {
            saveSkus(goodsId, skus, specValueIds, body);
        }
    }

    private void deleteGoodsDetail(Long goodsId) {
        jdbcTemplate.update("""
                delete from sku_spec_value
                where sku_id in (select id from sku where goods_id = ?)
                """, goodsId);
        jdbcTemplate.update("""
                delete from sku
                where goods_id = ?
                  and id not in (select sku_id from order_sku where goods_id = ?)
                  and id not in (select sku_id from cart_item where goods_id = ?)
                """, goodsId, goodsId, goodsId);
        jdbcTemplate.update("""
                update sku
                set status = 0,
                    sku_code = concat('ARCHIVED-', id, '-', left(coalesce(sku_code, ''), 80)),
                    updated_at = current_timestamp
                where goods_id = ?
                  and (
                    id in (select sku_id from order_sku where goods_id = ?)
                    or id in (select sku_id from cart_item where goods_id = ?)
                  )
                  and (sku_code is null or sku_code not like 'ARCHIVED-%')
                """, goodsId, goodsId, goodsId);
        jdbcTemplate.update("""
                delete from spec_value
                where spec_id in (select id from spec where goods_id = ?)
                """, goodsId);
        jdbcTemplate.update("delete from spec where goods_id = ?", goodsId);
        jdbcTemplate.update("delete from goods_property where goods_id = ?", goodsId);
        jdbcTemplate.update("delete from goods_picture where goods_id = ?", goodsId);
    }

    private void saveGoodsPictures(Long goodsId, List<Map<String, Object>> pictures, int type) {
        for (int index = 0; index < pictures.size(); index++) {
            String url = stringValue(pictures.get(index).get("url"));
            if (url.isBlank()) {
                continue;
            }
            jdbcTemplate.update("""
                    insert into goods_picture(goods_id, picture_url, picture_type, sort_order)
                    values (?, ?, ?, ?)
                    """, goodsId, url, type, sortOrder(pictures.get(index), index));
        }
    }

    private void saveProperties(Long goodsId, List<Map<String, Object>> properties) {
        for (int index = 0; index < properties.size(); index++) {
            Map<String, Object> property = properties.get(index);
            String name = stringValue(property.get("name"));
            String value = stringValue(property.get("value"));
            if (name.isBlank() || value.isBlank()) {
                continue;
            }
            jdbcTemplate.update("""
                    insert into goods_property(goods_id, name, value, sort_order)
                    values (?, ?, ?, ?)
                    """, goodsId, name, value, sortOrder(property, index));
        }
    }

    private Map<String, Map<String, Long>> saveSpecs(Long goodsId, List<Map<String, Object>> specs) {
        Map<String, Map<String, Long>> specValueIds = new HashMap<>();
        for (int specIndex = 0; specIndex < specs.size(); specIndex++) {
            Map<String, Object> spec = specs.get(specIndex);
            String specName = stringValue(spec.get("name"));
            if (specName.isBlank()) {
                continue;
            }
            int currentSpecIndex = specIndex;
            KeyHolder specKeyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        insert into spec(goods_id, name, sort_order)
                        values (?, ?, ?)
                        """, new String[]{"id"});
                statement.setLong(1, goodsId);
                statement.setString(2, specName);
                statement.setInt(3, sortOrder(spec, currentSpecIndex));
                return statement;
            }, specKeyHolder);
            Long specId = specKeyHolder.getKey().longValue();
            Map<String, Long> values = new HashMap<>();
            List<Map<String, Object>> specValues = listValue(spec.get("values"));
            for (int valueIndex = 0; valueIndex < specValues.size(); valueIndex++) {
                Map<String, Object> value = specValues.get(valueIndex);
                String valueName = stringValue(value.get("name"));
                if (valueName.isBlank()) {
                    continue;
                }
                int currentValueIndex = valueIndex;
                KeyHolder valueKeyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement("""
                            insert into spec_value(spec_id, name, picture, sort_order)
                            values (?, ?, ?, ?)
                            """, new String[]{"id"});
                    statement.setLong(1, specId);
                    statement.setString(2, valueName);
                    statement.setString(3, stringValue(value.get("picture")));
                    statement.setInt(4, sortOrder(value, currentValueIndex));
                    return statement;
                }, valueKeyHolder);
                values.put(valueName, valueKeyHolder.getKey().longValue());
            }
            specValueIds.put(specName, values);
        }
        return specValueIds;
    }

    private void saveSkus(Long goodsId, List<Map<String, Object>> skus, Map<String, Map<String, Long>> specValueIds, Map<String, Object> body) {
        for (int index = 0; index < skus.size(); index++) {
            Map<String, Object> sku = skus.get(index);
            int currentIndex = index;
            KeyHolder skuKeyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement("""
                        insert into sku(goods_id, sku_code, price, old_price, inventory, picture, status)
                        values (?, ?, ?, ?, ?, ?, ?)
                        """, new String[]{"id"});
                statement.setLong(1, goodsId);
                statement.setString(2, skuCode(goodsId, sku, currentIndex));
                statement.setBigDecimal(3, decimalValueOrDefault(sku.get("price"), decimalValue(body.get("price"))));
                statement.setBigDecimal(4, decimalValueOrDefault(sku.get("oldPrice"), decimalValue(body.get("oldPrice"))));
                statement.setInt(5, intValue(sku.get("inventory"), intValue(body.get("inventory"), 0)));
                statement.setString(6, stringValue(sku.get("picture")).isBlank() ? stringValue(body.get("picture")) : stringValue(sku.get("picture")));
                statement.setInt(7, intValue(sku.get("status"), 1));
                return statement;
            }, skuKeyHolder);
            Long skuId = skuKeyHolder.getKey().longValue();
            for (Map<String, Object> spec : listValue(sku.get("specs"))) {
                String specName = stringValue(spec.get("name"));
                String valueName = stringValue(spec.get("valueName"));
                Long specValueId = specValueIds.getOrDefault(specName, Map.of()).get(valueName);
                if (specName.isBlank() || valueName.isBlank() || specValueId == null) {
                    continue;
                }
                Long specId = jdbcTemplate.queryForObject("""
                        select spec_id from spec_value where id = ?
                        """, Long.class, specValueId);
                jdbcTemplate.update("""
                        insert into sku_spec_value(sku_id, spec_id, spec_name, spec_value_id, value_name)
                        values (?, ?, ?, ?, ?)
                        """, skuId, specId, specName, specValueId, valueName);
            }
        }
    }

    private void fillPrimaryPicture(Map<String, Object> body) {
        if (!stringValue(body.get("picture")).isBlank()) {
            return;
        }
        List<Map<String, Object>> mainPictures = listValue(body.get("mainPictures"));
        if (!mainPictures.isEmpty()) {
            body.put("picture", stringValue(mainPictures.get(0).get("url")));
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listValue(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private int sortOrder(Map<String, Object> row, int index) {
        return intValue(row.get("sortOrder"), index + 1);
    }

    private String skuCode(Long goodsId, Map<String, Object> sku, int index) {
        String code = stringValue(sku.get("skuCode"));
        return code.isBlank() ? "GOODS-" + goodsId + "-SKU-" + (index + 1) : code;
    }

    private BigDecimal decimalValueOrDefault(Object value, BigDecimal defaultValue) {
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        return decimalValue(value);
    }

    private Long nullableLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }
        return longValue(value);
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private int intValue(Object value, int defaultValue) {
        if (value == null || String.valueOf(value).isBlank()) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private BigDecimal decimalValue(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        return new BigDecimal(String.valueOf(value));
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
