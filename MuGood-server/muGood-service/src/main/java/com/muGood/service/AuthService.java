package com.muGood.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.muGood.common.exception.BizException;
import com.muGood.domain.entity.User;
import com.muGood.infrastructure.mapper.UserMapper;
import com.muGood.service.dto.LoginRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.muGood.service.support.ImageUrlSupport.normalizeImageUrl;
import static com.muGood.service.support.PasswordHashSupport.matches;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;

    public AuthService(UserMapper userMapper, JdbcTemplate jdbcTemplate) {
        this.userMapper = userMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getAccount, request.account())
                .eq(User::getStatus, 1)
                .last("limit 1"));
        if (user == null) {
            throw new BizException("AUTH_001", "账号或密码错误");
        }
        if (user.getPasswordHash() != null
                && user.getPasswordHash().startsWith("sha256$")
                && !matches(request.password(), user.getPasswordHash())) {
            throw new BizException("AUTH_001", "账号或密码错误");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("account", user.getAccount());
        result.put("nickname", user.getNickname());
        result.put("avatar", normalizeImageUrl(user.getAvatar()));
        result.put("mobile", user.getMobile());
        result.put("token", demoToken(user));
        return result;
    }

    public Map<String, Object> adminLogin(LoginRequest request) {
        List<Map<String, Object>> admins = jdbcTemplate.queryForList("""
                select id, account, password_hash passwordHash, nickname, avatar, mobile, email, role
                from admin
                where account = ? and status = 1
                limit 1
                """, request.account());
        if (admins.isEmpty() || !passwordMatches(request.password(), String.valueOf(admins.get(0).get("passwordHash")))) {
            throw new BizException("AUTH_001", "账号或密码错误");
        }

        Map<String, Object> admin = admins.get(0);
        jdbcTemplate.update("update admin set last_login_at = current_timestamp where id = ?", admin.get("id"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", admin.get("id"));
        result.put("account", admin.get("account"));
        result.put("nickname", admin.get("nickname"));
        result.put("avatar", normalizeImageUrl((String) admin.get("avatar")));
        result.put("mobile", admin.get("mobile"));
        result.put("email", admin.get("email"));
        result.put("role", admin.get("role"));
        result.put("token", adminToken(admin));
        return result;
    }

    private String demoToken(User user) {
        String payload = user.getId() + ":" + user.getAccount();
        return Base64.getUrlEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private String adminToken(Map<String, Object> admin) {
        String payload = "admin:" + admin.get("id") + ":" + admin.get("account");
        return Base64.getUrlEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (storedPassword != null && storedPassword.startsWith("sha256$")) {
            return matches(rawPassword, storedPassword);
        }
        return storedPassword != null && storedPassword.equals(rawPassword);
    }
}
