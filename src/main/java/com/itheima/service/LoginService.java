package com.itheima.service;

public interface LoginService {

    // 校验口令并签发令牌，同时写入 Redis 白名单
    String login(String username, String rawPassword);

    // 服务端登出：删除 Redis 中的令牌
    void logout(String token);

    // 修改密码：校验旧口令与新口令规则，并让该用户所有会话失效
    void changePassword(Integer userId, String username, String oldPwd, String newPwd, String rePwd);
}
