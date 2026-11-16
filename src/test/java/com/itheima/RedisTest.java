package com.itheima;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void testSet() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("name", "zhangsan");

        String value = ops.get("name");
        System.out.println("name=" + value);
    }
}
/*
 * @ParameterizedTest --> 参数化测试(可以让单个测试运行多次，每次运行时仅参数不同，配合@ValueSource使用)
 * @ValueSource --> 参数源，赋予测试方法参数
 * @BeforeEach --> 每个测试方法运行之前执行一次
 * @AfterEach --> 每个测试方法运行之后执行一次
 * @BeforeAll --> 所有测试方法运行之前执行一次 (静态方法)
 * @AfterAll --> 所有测试方法运行之后执行一次 (静态方法)
 * @DisplayName --> 为测试类或者测试方法指定一个名称
 */