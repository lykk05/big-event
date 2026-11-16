package com.itheima.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息出参：不包含口令等敏感字段。
 */
@Data
public class UserProfileVO {

    private Integer id;
    private String username;
    private String nickname;
    private String email;
    private String userPic;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static UserProfileVO from(User user) {
        if (user == null) {
            return null;
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setUserPic(user.getUserPic());
        vo.setCreateTime(user.getCreateTime());
        vo.setUpdateTime(user.getUpdateTime());
        return vo;
    }
}
