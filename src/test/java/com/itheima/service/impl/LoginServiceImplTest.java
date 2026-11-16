package com.itheima.service.impl;

import com.itheima.exception.BizException;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.Md5Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private LoginServiceImpl loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginServiceImpl(userService, redisTemplate, passwordEncoder, jwtUtil);
    }

    @Test
    void loginWithBcryptPasswordIssuesToken() {
        User user = user(1, "$2a$10$abcdefghijklmnopqrstuv");
        when(userService.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("Pass12345", user.getPassword())).thenReturn(true);
        when(jwtUtil.genToken(anyMap())).thenReturn("token");
        when(jwtUtil.getTtlHours()).thenReturn(2L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        String token = loginService.login("alice", "Pass12345");

        assertEquals("token", token);
        verify(valueOperations).set("token", "1", 2L, TimeUnit.HOURS);
        verify(setOperations).add("user:tokens:1", "token");
        verify(redisTemplate).expire("user:tokens:1", 2L, TimeUnit.HOURS);
    }

    @Test
    void loginWithWrongPasswordThrows() {
        User user = user(1, "$2a$10$abcdefghijklmnopqrstuv");
        when(userService.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        BizException ex = assertThrows(BizException.class, () -> loginService.login("alice", "wrong"));

        assertEquals("密码错误", ex.getMessage());
    }

    @Test
    void loginWithLegacyMd5UpgradesHash() {
        User user = user(1, Md5Util.getMD5String("Pass12345"));
        when(userService.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.encode("Pass12345")).thenReturn("$2a$encoded");
        when(jwtUtil.genToken(anyMap())).thenReturn("token");
        when(jwtUtil.getTtlHours()).thenReturn(2L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        loginService.login("alice", "Pass12345");

        verify(userService).updatePwdHash(1, "$2a$encoded");
    }

    @Test
    void changePasswordInvalidatesAllTokens() {
        User user = user(1, "$2a$10$abcdefghijklmnopqrstuv");
        when(userService.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("Pass12345", user.getPassword())).thenReturn(true);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("user:tokens:1")).thenReturn(Set.of("t1", "t2"));

        loginService.changePassword(1, "alice", "Pass12345", "Pass12346", "Pass12346");

        verify(userService).updatePwd(1, "Pass12346");
        verify(redisTemplate).delete(Set.of("t1", "t2"));
        verify(redisTemplate).delete("user:tokens:1");
    }

    @Test
    void changePasswordRejectsMismatchedConfirmation() {
        User user = user(1, "$2a$10$abcdefghijklmnopqrstuv");
        when(userService.findByUsername("alice")).thenReturn(user);
        when(passwordEncoder.matches("Pass12345", user.getPassword())).thenReturn(true);

        BizException ex = assertThrows(BizException.class,
                () -> loginService.changePassword(1, "alice", "Pass12345", "Pass12346", "Pass99999"));

        assertEquals("两次输入的密码不一致", ex.getMessage());
    }

    private User user(int id, String password) {
        User user = new User();
        user.setId(id);
        user.setUsername("alice");
        user.setPassword(password);
        return user;
    }
}
