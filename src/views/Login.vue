<script setup>
import { User, Lock } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
//控制注册与登录表单的显示， 默认显示登录
const isRegister = ref(false)
// 表单实例，提交前统一调用 validate()
const formRef = ref()
// 定义数据模型
const registerData = ref({
    username: '',
    password: '',
    confirmPassword: '',
})

const checkRePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== registerData.value.password) {
        callback(new Error('两次输入密码不一致'))
    } else {
        callback()
    }
}

// 定义表单校验规则
const rules = {
    username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { pattern: /^\S{2,16}$/, message: '用户名必须为2-16位且不能包含空格', trigger: 'blur' }
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { pattern: /^\S{5,16}$/, message: '密码必须为5-16位且不能包含空格', trigger: 'blur' }
    ],
    confirmPassword: [
        { validator: checkRePassword, trigger: 'blur' }
    ]
}

// 提交前先做本地校验，避免把空值/带空格的值发给后端
const validateForm = async () => {
    if (!formRef.value) return false
    try {
        await formRef.value.validate()
        return true
    } catch (e) {
        return false
    }
}

// 调用后台接口，完成接口注册
import { userRegisterService } from '@/api/user.js'
const register = async () => {
    if (!(await validateForm())) return
    let res = await userRegisterService(registerData.value)
    ElMessage.success(res.message? res.message : '注册成功')
    isRegister.value = false
    clearRegisterData()
}

// 绑定数据，复用注册表单数据
// 表单数据校验
// 登录函数
import { userLoginService } from '@/api/user.js'
import { useRouter } from 'vue-router'
const router = useRouter() // 
import { useTokenStore } from '@/stores/token.js'
const tokenStore = useTokenStore()
const login = async () => {
    if (!(await validateForm())) return
    let res = await userLoginService(registerData.value)
    ElMessage.success(res.message? res.message : '登录成功')
    // 把得到的token存到tokenStore中
    tokenStore.setToken(res.data)
    // 跳转到首页
    router.push('/')
}

// 定义函数，清空数据模型的数据
const clearRegisterData = () => {
    registerData.value = {
        username: '',
        password: '',
        confirmPassword: '',
    }
}

</script>

<template>
    <el-row class="login-page">
        <el-col :span="12" class="bg"></el-col>
        <el-col :span="6" :offset="3" class="form">
            <!-- 注册表单 -->
            <el-form ref="formRef" size="large" autocomplete="off" v-if="isRegister" :model="registerData" :rules="rules">
                <el-form-item>
                    <h1>注册</h1>
                </el-form-item>
                <el-form-item prop="username">
                    <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.username"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入密码" v-model="registerData.password"></el-input>
                </el-form-item>
                <el-form-item prop="confirmPassword">
                    <el-input :prefix-icon="Lock" type="password" placeholder="请输入再次密码" v-model="registerData.confirmPassword"></el-input>
                </el-form-item>
                <!-- 注册按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" auto-insert-space @click="register">
                        注册
                    </el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = false;clearRegisterData()">
                        ← 返回
                    </el-link>
                </el-form-item>
            </el-form>
            <!-- 登录表单 -->
            <el-form ref="formRef" size="large" autocomplete="off" v-else :model="registerData" :rules="rules">
                <el-form-item>
                    <h1>登录</h1>
                </el-form-item>
                <el-form-item prop="username">
                    <el-input :prefix-icon="User" placeholder="请输入用户名" v-model="registerData.username"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input name="password" :prefix-icon="Lock" type="password" placeholder="请输入密码" v-model="registerData.password"></el-input>
                </el-form-item>
                <el-form-item class="flex">
                    <div class="flex">
                        <el-checkbox>记住我</el-checkbox>
                        <el-link type="primary" :underline="false">忘记密码？</el-link>
                    </div>
                </el-form-item>
                <!-- 登录按钮 -->
                <el-form-item>
                    <el-button class="button" type="primary" auto-insert-space @click="login">登录</el-button>
                </el-form-item>
                <el-form-item class="flex">
                    <el-link type="info" :underline="false" @click="isRegister = true; clearRegisterData()">
                        注册 →
                    </el-link>
                </el-form-item>
            </el-form>
        </el-col>
    </el-row>
</template>

<style lang="scss" scoped>
.login-page {
    height: 100vh;
    background: linear-gradient(135deg, #e8f4f8 0%, #f0e6f6 50%, #fce4ec 100%);

    .bg {
        background:
            url('@/assets/logo2.png') no-repeat 60% center / 240px auto,
            url('@/assets/login_bg.jpg') no-repeat center / cover;
        border-radius: 0 20px 20px 0;
    }

    .form {
        display: flex;
        flex-direction: column;
        justify-content: center;
        user-select: none;

        :deep(.el-form) {
            background: #fff;
            padding: 40px 36px;
            border-radius: 16px;
            box-shadow: 0 8px 40px rgba(0, 0, 0, 0.08);
            animation: fadeIn 0.4s ease;
        }

        h1 {
            text-align: center;
            font-size: 26px;
            color: #303133;
            letter-spacing: 6px;
            margin: 0;
            padding-bottom: 12px;
            position: relative;

            &::after {
                content: '';
                position: absolute;
                bottom: 0;
                left: 50%;
                transform: translateX(-50%);
                width: 40px;
                height: 3px;
                border-radius: 2px;
                background: linear-gradient(90deg, #409eff, #67c23a);
            }
        }

        :deep(.el-input__wrapper) {
            border-radius: 8px;
            box-shadow: 0 0 0 1px #dcdfe6 inset;
            transition: box-shadow 0.3s;

            &:hover {
                box-shadow: 0 0 0 1px #c0c4cc inset;
            }
        }

        .button {
            width: 100%;
            letter-spacing: 4px;
            border-radius: 8px;
            height: 44px;
            font-size: 16px;
            transition: all 0.3s;

            &:hover {
                transform: translateY(-1px);
                box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
            }
        }

        .flex {
            width: 100%;
            display: flex;
            justify-content: space-between;
        }
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(12px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>