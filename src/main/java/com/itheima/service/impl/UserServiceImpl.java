package com.itheima.service.impl;

import com.itheima.mapper.UserMapper;
import com.itheima.pojo.User;
import com.itheima.pojo.UserUpdateDTO;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 重写查询用户方法
    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    // 重写注册方法
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String username, String password) {
        userMapper.add(username, passwordEncoder.encode(password));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCurrentUser(UserUpdateDTO userUpdateDTO, Integer userId) {
        User user = new User();
        user.setId(userId);                 // id 只来自登录上下文，不采信请求体
        user.setNickname(userUpdateDTO.getNickname());
        user.setEmail(userUpdateDTO.getEmail());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAvatar(String avatarUrl, Integer userId) {
        userMapper.updateAvatar(avatarUrl, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePwd(Integer userId, String newPwd) {
        userMapper.updatePwd(passwordEncoder.encode(newPwd), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePwdHash(Integer userId, String encodedPassword) {
        // 已经是 BCrypt 散列，不能再做一次 MD5
        userMapper.updatePwd(encodedPassword, userId);
    }
}
