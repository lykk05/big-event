package com.itheima.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final Algorithm algorithm;
    private final long ttlHours;

    public JwtUtil(@Value("${jwt.key}") String key,
                   @Value("${jwt.ttl-hours:2}") long ttlHours) {
        this.algorithm = Algorithm.HMAC256(key);
        this.ttlHours = ttlHours;
    }

    // 接收业务数据,生成token并返回
    public String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + ttlHours * 3600_000L))
                .sign(algorithm);
    }

    // 接收token,验证token,并返回业务数据
    public Map<String, Object> parseToken(String token) {
        return JWT.require(algorithm)
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

    public long getTtlHours() {
        return ttlHours;
    }
}
