package com.itheima.service;

import com.itheima.pojo.User;
import com.itheima.pojo.UserUpdateDTO;

public interface UserService {
    // 根据用户名查询用户
    User findByUsername(String username);

    // 注册
    void register(String username, String password);

    // 更新指定用户的资料（userId 由调用方显式传入）
    void updateCurrentUser(UserUpdateDTO userUpdateDTO, Integer userId);

    // 更新头像
    void updateAvatar(String avatarUrl, Integer userId);

    // 修改密码（入参为明文，内部加密）
    void updatePwd(Integer userId, String newPwd);

    // 直接写入已加密的口令散列（用于存量 MD5 登录后的静默升级）
    void updatePwdHash(Integer userId, String encodedPassword);
}
