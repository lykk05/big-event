package com.itheima.controller;

import com.itheima.anno.PublicApi;
import com.itheima.pojo.Result;
import com.itheima.pojo.UpdatePasswordDTO;
import com.itheima.pojo.User;
import com.itheima.pojo.UserProfileVO;
import com.itheima.pojo.UserUpdateDTO;
import com.itheima.service.LoginService;
import com.itheima.service.UserService;
import com.itheima.utils.ThreadLocalUtil;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    // 该接口用于注册新用户
    @PublicApi
    @PostMapping("/register")
    public Result register(@Pattern(regexp = "^\\S{5,16}$") String username,
                           @Pattern(regexp = "^\\S{5,16}$") String password) {
        User u = userService.findByUsername(username);
        if (u == null) {
            userService.register(username, password);
            return Result.success();
        }
        return Result.error("用户名被占用");
    }

    // 该接口用于登录
    @PublicApi
    @PostMapping("/login")
    public Result<String> login(@Pattern(regexp = "^\\S{5,16}$") String username,
                                @Pattern(regexp = "^\\S{5,16}$") String password) {
        return Result.success(loginService.login(username, password));
    }

    // 该接口用于退出登录（服务端删除令牌）
    @PostMapping("/logout")
    public Result logout(@RequestHeader("Authorization") String token) {
        loginService.logout(token);
        return Result.success();
    }

    // 该接口用于获取当前已登录用户的详细信息
    @GetMapping("/userInfo")
    public Result<UserProfileVO> userInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        String username = (String) map.get("username");
        return Result.success(UserProfileVO.from(userService.findByUsername(username)));
    }

    // 该接口用于更新已登录用户的基本信息(除头像和密码)
    @PutMapping("/update")
    public Result update(@RequestBody @Validated UserUpdateDTO userUpdateDTO) {
        userService.updateCurrentUser(userUpdateDTO, currentUserId());
        return Result.success();
    }

    // 该接口用于更新已登录用户的头像
    @PatchMapping("/updateAvatar")
    public Result updateAvatar(@RequestParam @URL String avatarUrl) {
        userService.updateAvatar(avatarUrl, currentUserId());
        return Result.success();
    }

    // 该接口用于更新已登录用户的密码
    @PatchMapping("/updatePwd")
    public Result updatePassword(@RequestBody @Validated UpdatePasswordDTO dto,
                                 @RequestHeader("Authorization") String token) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Integer userId = (Integer) map.get("id");
        String username = (String) map.get("username");
        loginService.changePassword(userId, username, dto.getOldPwd(), dto.getNewPwd(), dto.getRePwd());
        return Result.success();
    }

    private Integer currentUserId() {
        Map<String, Object> map = ThreadLocalUtil.get();
        return (Integer) map.get("id");
    }
}
