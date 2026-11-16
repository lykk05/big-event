// 导入request请求工具
import request from '@/utils/request.js'

// 提供调用注册接口的函数
export const userRegisterService = (registerData)=>{
    // 借助 URLSearchParams 完成参数传递
    const params = new URLSearchParams()
    for(let key in registerData) {
        params.append(key,registerData[key]);
    }
    return request.post('/user/register',params);
}

// 提供调用登录接口的函数
export const userLoginService = (loginData)=>{
    // 借助 URLSearchParams 完成参数传递
    const params = new URLSearchParams()
    for(let key in loginData) {
        params.append(key,loginData[key]);
    }
    return request.post('/user/login',params);
}

// 提供调用获取用户信息接口的函数
export const userInfoService = () => {
    return request.get('/user/userInfo')
}

// 修改个人信息的函数
export const userInfoUpdateService = (updateData) => {
    return request.put('/user/update',updateData)
}

// 修改头像的接口函数
export const userAvatarUpdateService = (avatarUrl) => {
    const params = new URLSearchParams()
    params.append('avatarUrl',avatarUrl)
    return request.patch('/user/updateAvatar',params)
}

// 修改密码的接口函数
export const userPasswordUpdateService = (passwordData) => {
    return request.patch('/user/updatePwd',passwordData)
}

// 退出登录的接口函数（服务端删除令牌）
export const userLogoutService = () => {
    return request.post('/user/logout')
}
