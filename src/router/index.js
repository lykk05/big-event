import { createRouter, createWebHistory } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
// 导入登录组件和布局组件
import LoginVue from '../views/Login.vue'
import LayoutVue from '../views/Layout.vue'

// 导入文章分类、文章管理、用户信息、用户头像、用户重置密码组件
import ArticleCategoryVue from '../views/article/ArticleCategory.vue'
import ArticleManageVue from '../views/article/ArticleManage.vue'
import ArticleEditorVue from '../views/article/ArticleEditor.vue'
import UserInfoVue from '../views/user/UserInfo.vue'
import UserAvatarVue from '../views/user/UserAvatar.vue'
import UserResetPasswordVue from '../views/user/UserResetPassword.vue'


// 定义路由配置
const routes = [
    {path: '/login', component: ()=> import("../views/Login.vue")},
    {path: '/', 
    component: LayoutVue,
    // 重定向
    redirect: '/article/manage',
    // 子路由
    children:[
        {path: '/article/category', component: ArticleCategoryVue},
        {path: '/article/manage', component: ArticleManageVue},
        {path: '/article/editor', component: ArticleEditorVue},
        {path: '/user/info', component: UserInfoVue},
        {path: '/user/avatar', component: UserAvatarVue},
        {path: '/user/resetPassword', component: UserResetPasswordVue}
    ]}
]

// 创建路由器
const router = createRouter({
    history: createWebHistory(),
    routes: routes
})

// 未登录时不要进入受保护页面，避免页面同时发多个请求导致 401 提示刷屏
router.beforeEach((to) => {
    const tokenStore = useTokenStore()
    if (to.path !== '/login' && !tokenStore.token) {
        return '/login'
    }
})

// 导出路由器
export default router