<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter} from 'vue-router'
import { userPasswordUpdateService } from '@/api/user.js'
import { useTokenStore } from '@/stores/token.js'
import useUserInfoStore from '@/stores/userInfo.js'

const router = useRouter()
const tokenStore = useTokenStore()
const userInfoStore = useUserInfoStore()


const pwdForm = ref({
  old_pwd: '',
  new_pwd: '',
  re_pwd: ''
})

const checkRePwd = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== pwdForm.value.new_pwd) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  old_pwd: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 5, max: 16, message: '密码长度为5到16位', trigger: 'blur' }
  ],
  new_pwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 5, max: 16, message: '密码长度为5到16位', trigger: 'blur' }
  ],
  re_pwd: [
    { validator: checkRePwd, trigger: 'blur' }
  ]
}



// 修改密码的函数
const userUpdatePassword = async () => {
    const result = await userPasswordUpdateService(pwdForm.value) 
    ElMessage.success(result.message ? result.message : '修改成功')
    // 清除登录信息
    tokenStore.removeToken()
    userInfoStore.clearUserInfo()
    // 跳转登录页
    router.push('/login')
}

// 重置表单
const resetForm = () => { 
    pwdForm.value = { 
        old_pwd: '', 
        new_pwd: '', 
        re_pwd: '' 
    } 
}

</script>
<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span>修改密码</span>
            </div>
        </template>
        <el-row>
            <el-col :span="12">
                <el-form :model="pwdForm" :rules="rules" label-width="100px" size="large">
                    <el-form-item label="原密码" prop="old_pwd">
                        <el-input v-model="pwdForm.old_pwd" type="password"></el-input>
                    </el-form-item>
                    <el-form-item label="新密码" prop="new_pwd">
                        <el-input v-model="pwdForm.new_pwd" type="password"></el-input>
                    </el-form-item>
                    <el-form-item label="确认新密码" prop="re_pwd">
                        <el-input v-model="pwdForm.re_pwd" type="password"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="userUpdatePassword">提交修改</el-button>
                        <el-button @click="resetForm">重置</el-button>
                    </el-form-item>
                </el-form>
            </el-col>
        </el-row>
    </el-card>
</template>