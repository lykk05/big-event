import { defineStore } from 'pinia'
import { ref } from 'vue'

const useUserInfoStore =  defineStore('userInfo', ()=> {
    // 定义状态相关内容
    const userInfo = ref({})

    // 定义用户信息方法
    const setUserInfo = (newInfo) => {
        userInfo.value = newInfo
    }

    // 定义清除用户信息方法
    const clearUserInfo = () => {
        userInfo.value = {}
    }

    // 返回状态和操作方法
    return {userInfo, setUserInfo, clearUserInfo}
}, {persist: true})

export default useUserInfoStore