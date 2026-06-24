package com.itheima.controller;
/*
* Controller层: 对外接口入口，只和HTTP协议打交道。负责接收请求，调用service层处理业务逻辑，mapper层处理数据，封装响应数据返回给前端。
* 最核心的调用关系：

```text
前端页面
  ↓
src/api 发送 HTTP 请求
  ↓
Interceptor 拦截并校验
  ↓
Controller 接收参数
  ↓
Service 处理业务
  ↓
Mapper 执行 SQL
  ↓
MySQL
  ↓
返回查询结果
  ↓
Service 继续处理
  ↓
Controller 返回 Result
  ↓
前端展示
```

* */

import com.itheima.pojo.Result;
import com.itheima.pojo.User;
import com.itheima.service.UserService;
import com.itheima.utils.JwtUtil;
import com.itheima.utils.Md5Util;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 该接口用于注册新用户
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username,@Pattern(regexp = "^\\S{5,16}$") String password){
        // 下面用spring validation校验
        // 查询用户
        User u = userService.findByUsername(username);
        if(u == null){
            // 没有被占用
            // 注册
            userService.register(username, password);
            return Result.success();
        }else {
            // 被占用了
            return Result.error("用户名被占用");
        }
    }

    // 该接口用于登录
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username,@Pattern(regexp = "^\\S{5,16}$") String password){
        // 根据用户名查询用户
        User loginUser = userService.findByUsername(username);
        // 判断用户是否存在
        if (loginUser == null){
            return Result.error("用户不存在");
        }
        // 密码是否正确 --> 这里的loginUser.getPassword()是数据库中的密码,被加密过
        if (loginUser.getPassword().equals(Md5Util.getMD5String(password))){
            // 登录成功
            // 生成一个Map集合，里面存放用户信息 id 密码
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            claims.put("password", loginUser.getPassword());
            String token = JwtUtil.genToken(claims);
            // 把token存储到redis中
            ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
            operations.set(token, token, 1, TimeUnit.HOURS);
            return Result.success(token);
        }
        return Result.error("密码错误");

    }

    // 该接口用于获取当前已登录用户的详细信息
    @GetMapping("/userInfo")
    public Result<User> userInfo(/*@RequestHeader( name = "Authorization") String token*/){
        // 验证token
        // Map<String, Object> map = JwtUtil.parseToken(token);
        Map<String, Object> map = ThreadLocalUtil.get();
        // 根据用户名查询用户
        String username = (String)map.get("username");
        User user = userService.findByUsername(username);
        return Result.success(user);
    }

    // 该接口用于更新已登录用户的基本信息(除头像和密码)
    @PutMapping("/update")
    public Result update(@RequestBody @Validated User user){
        userService.update(user);
        return Result.success();
    }

    // 该接口用于更新已登录用户的头像
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl){
        userService.updateAvatar(avatarUrl);
        return Result.success();
    }

    // 该接口用于更新已登录用户的密码
    @PatchMapping("/updatePwd")
    public Result updatePassword(@RequestBody Map<String, String> params, @RequestHeader( name = "Authorization") String token){
        // 校验参数
        String oldPwd = params.get("old_pwd");
        String newPwd = params.get("new_pwd");
        String rePwd = params.get("re_pwd");

        if(!StringUtils.hasLength(oldPwd) || !StringUtils.hasLength(newPwd) || !StringUtils.hasLength(rePwd)){
            return Result.error("缺少必要的参数");
        }

        // 原密码是否正确
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String)map.get("username");
        User loginUser = userService.findByUsername(username);
        if(!loginUser.getPassword().equals(Md5Util.getMD5String(oldPwd))){
            return Result.error("原密码错误");
        }

        if(!newPwd.equals(rePwd)){
            return Result.error("两次输入的密码不一致");
        }

        // 调用 service完成密码更新
        userService.updatePwd(newPwd);
        // 删除redis中对应的token
        ValueOperations<String, String> operations = stringRedisTemplate.opsForValue();
        operations.getOperations().delete(token);
        return Result.success();

    }

}
