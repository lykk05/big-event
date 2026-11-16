package com.itheima.service.impl;

import com.itheima.exception.BizException;
import com.itheima.pojo.User;
import com.itheima.service.LoginService;
import com.itheima.service.UserService;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.Md5Util;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String USER_TOKEN_KEY_PREFIX = "user:tokens:";
    private static final String PASSWORD_PATTERN = "^\\S{5,16}$";

    private final UserService userService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginServiceImpl(UserService userService,
                            StringRedisTemplate redisTemplate,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public String login(String username, String rawPassword) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!passwordMatches(rawPassword, user.getPassword())) {
            throw new BizException("密码错误");
        }

        // 存量无盐 MD5 在登录成功后静默升级为 BCrypt
        if (!isBcrypt(user.getPassword())) {
            userService.updatePwdHash(user.getId(), passwordEncoder.encode(rawPassword));
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        String token = jwtUtil.genToken(claims);

        long ttlHours = jwtUtil.getTtlHours();
        redisTemplate.opsForValue().set(token, String.valueOf(user.getId()), ttlHours, TimeUnit.HOURS);
        String userTokensKey = USER_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForSet().add(userTokensKey, token);
        redisTemplate.expire(userTokensKey, ttlHours, TimeUnit.HOURS);
        return token;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        String userId = redisTemplate.opsForValue().get(token);
        redisTemplate.delete(token);
        if (userId != null) {
            redisTemplate.opsForSet().remove(USER_TOKEN_KEY_PREFIX + userId, token);
        }
    }

    @Override
    public void changePassword(Integer userId, String username, String oldPwd, String newPwd, String rePwd) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!user.getId().equals(userId)) {
            throw new BizException(403, "无权操作");
        }
        if (!passwordMatches(oldPwd, user.getPassword())) {
            throw new BizException("原密码错误");
        }
        if (!newPwd.equals(rePwd)) {
            throw new BizException("两次输入的密码不一致");
        }
        if (newPwd.equals(oldPwd)) {
            throw new BizException("新密码不能与原密码相同");
        }
        if (!newPwd.matches(PASSWORD_PATTERN)) {
            throw new BizException("新密码长度为5-16位，不能包含空格");
        }

        userService.updatePwd(userId, newPwd);
        invalidateAllTokens(userId);
    }

    // 让该用户在所有设备上的会话失效
    private void invalidateAllTokens(Integer userId) {
        String key = USER_TOKEN_KEY_PREFIX + userId;
        Set<String> tokens = redisTemplate.opsForSet().members(key);
        if (tokens != null && !tokens.isEmpty()) {
            redisTemplate.delete(tokens);
        }
        redisTemplate.delete(key);
    }

    /**
     * 校验明文口令与库中散列是否匹配：兼容存量无盐 MD5 与 BCrypt。
     */
    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (isBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return Md5Util.getMD5String(rawPassword).equals(storedPassword);
    }

    private boolean isBcrypt(String storedPassword) {
        return storedPassword != null
                && (storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$"));
    }
}
