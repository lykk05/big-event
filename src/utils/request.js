//定制请求的实例

//导入axios  npm install axios
import axios from 'axios';
import { ElMessage } from 'element-plus';
//定义一个变量,记录公共的前缀  ,  baseURL
const baseURL = '/api';
// 超时时间要长于后端 AI 读超时（默认 30s）
const instance = axios.create({baseURL, timeout: 35000})

import { useTokenStore } from '@/stores/token.js'
// 添加请求拦截器
instance.interceptors.request.use(
    (config) => { // 请求前的回调
        const tokenStore = useTokenStore()
        if (tokenStore.token) {
        config.headers.Authorization = tokenStore.token
        }
        return config
    },
    (err) => {
        // 请求错误的回调
        return Promise.reject(err)
    }
)

import router from '@/router';
//添加响应拦截器
instance.interceptors.response.use(
    result=>{
        // 判断业务状态码
        if(result.data.code === 0) { // 成功
            return result.data;
        }
        // 失败
        ElMessage.error(result.data.message? result.data.message : '操作失败');
        const businessError = new Error(result.data.message || '操作失败');
        businessError.code = result.data.code;
        return Promise.reject(businessError);
    },
    err=>{
        // 网络断开、超时、DNS 失败时 err.response 不存在
        if (!err.response) {
            if (err.code !== 'ERR_CANCELED') {
                ElMessage.error('网络异常，请检查网络后重试')
            }
            return Promise.reject(err)
        }
        // 判断响应状态码为401，证明未登录，提示请登录，并跳转到登录页
        if(err.response.status===401){
            ElMessage.error('请先登录')
            router.push('/login')
        }else{
            ElMessage.error(`服务异常（${err.response.status}）`);
        }
        return Promise.reject(err);//异步的状态转化成失败的状态
    }
)

export default instance;
