<script setup>
import {
    Edit,
    Delete,
    View,
    Link,
    Clock,
    CircleClose
} from '@element-plus/icons-vue'

import { ref } from 'vue'

//文章分类数据模型
const categorys = ref([
    {
        "id": 3,
        "categoryName": "美食",
        "categoryAlias": "my",
        "createTime": "2023-09-02 12:06:59",
        "updateTime": "2023-09-02 12:06:59"
    },
    {
        "id": 4,
        "categoryName": "娱乐",
        "categoryAlias": "yl",
        "createTime": "2023-09-02 12:08:16",
        "updateTime": "2023-09-02 12:08:16"
    },
    {
        "id": 5,
        "categoryName": "军事",
        "categoryAlias": "js",
        "createTime": "2023-09-02 12:08:33",
        "updateTime": "2023-09-02 12:08:33"
    }
])

//用户搜索时选中的分类id
const categoryId = ref('')

//用户搜索时选中的发布状态
const state = ref('')

//文章列表数据模型
const articles = ref([
    {
        "id": 5,
        "title": "陕西旅游攻略",
        "content": "兵马俑,华清池,法门寺,华山...爱去哪去哪...",
        "coverImg": "https://big-event-gwd.oss-cn-beijing.aliyuncs.com/9bf1cf5b-1420-4c1b-91ad-e0f4631cbed4.png",
        "state": "草稿",
        "categoryId": 2,
        "createTime": "2023-09-03 11:55:30",
        "updateTime": "2023-09-03 11:55:30"
    },
    {
        "id": 5,
        "title": "陕西旅游攻略",
        "content": "兵马俑,华清池,法门寺,华山...爱去哪去哪...",
        "coverImg": "https://big-event-gwd.oss-cn-beijing.aliyuncs.com/9bf1cf5b-1420-4c1b-91ad-e0f4631cbed4.png",
        "state": "草稿",
        "categoryId": 2,
        "createTime": "2023-09-03 11:55:30",
        "updateTime": "2023-09-03 11:55:30"
    },
    {
        "id": 5,
        "title": "陕西旅游攻略",
        "content": "兵马俑,华清池,法门寺,华山...爱去哪去哪...",
        "coverImg": "https://big-event-gwd.oss-cn-beijing.aliyuncs.com/9bf1cf5b-1420-4c1b-91ad-e0f4631cbed4.png",
        "state": "草稿",
        "categoryId": 2,
        "createTime": "2023-09-03 11:55:30",
        "updateTime": "2023-09-03 11:55:30"
    },
])

//分页条数据模型
const pageNum = ref(1)//当前页
const total = ref(20)//总条数
const pageSize = ref(3)//每页条数

//当每页条数发生了变化，调用此函数
const onSizeChange = (size) => {
    pageSize.value = size
    getArticleList()
}
//当前页码发生变化，调用此函数
const onCurrentChange = (num) => {
    pageNum.value = num
    getArticleList()
}

// 获取文章分类列表
import { articleCategoryListService, articleListService, articleAddService, articleUpdateService, articleDeleteService } from '@/api/article.js'
const articleCategoryList = async () => {
    let result = await articleCategoryListService()
    categorys.value = result.data
}

// 获取文章列表数据
const getArticleList = async () => {
    let params = {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        categoryId: categoryId.value ? categoryId.value : null,
        state: state.value ? state.value : null
    }
    let result = await articleListService(params)
    articles.value = result.data.items
    total.value = result.data.total
    // 构建分类映射表
    const categoryMap = new Map()

    categorys.value.forEach(item => {
        categoryMap.set(item.id, item.categoryName)
    })

    // 给文章添加分类名称
    articles.value.forEach(article => {
        article.categoryName = categoryMap.get(article.categoryId)
    })
}
articleCategoryList()
getArticleList()

import { Plus } from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'
//控制抽屉是否显示
const visibleDrawer = ref(false)
//添加表单数据模型
const articleModel = ref({
    title: '',
    categoryId: '',
    coverImg: '',
    content: '',
    state: ''
})

import { useTokenStore } from '@/stores/token.js'
const tokenStore = useTokenStore()
// 上传图片成功的回调函数
const uploadSuccess = (ressult) => {
    articleModel.value.coverImg = ressult.data
    console.log(ressult.data)
}

import { ElMessage,ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
const router = useRouter()
const isEdit = ref(false)

// 跳转到文章编辑页面（新增文章）
const goToAddArticle = () => {
    clearArticleModel()
    isEdit.value = false
    router.push('/article/editor')
}

// 添加文章函数
const addArticle = async (clickState) => {
    // 把发布状态赋值给数据模型
    articleModel.value.state = clickState
    if (isEdit.value) {
        // 调用修改文章的接口函数
        let result = await articleUpdateService(articleModel.value)
        ElMessage.success(result.message ? result.message : '修改成功')
    } else {
        // 调用添加文章的接口函数
        let result = await articleAddService(articleModel.value)
        ElMessage.success(result.message ? result.message : '添加成功')
    }
    // 隐藏抽屉
    visibleDrawer.value = false
    // 刷新文章列表
    getArticleList()
}

const editorKey = ref(0)
// 清空表单数据模型的函数
const clearArticleModel = () => {
    articleModel.value = {
        title: '',
        categoryId: '',
        coverImg: '',
        content: '',
        state: ''
    }
    editorKey.value++
}

// 在点击编辑根据点击的row显示不同的表单数据
const updateArticle = (row) => {
    articleModel.value.id = row.id
    articleModel.value.title = row.title
    articleModel.value.categoryId = row.categoryId
    articleModel.value.coverImg = row.coverImg
    articleModel.value.content = row.content
    articleModel.value.state = row.state
}

// 控制预览抽屉是否显示
const visiblePreviewDrawer = ref(false)
// 当前预览的文章数据
const previewArticle = ref({})

// 打开预览抽屉
const openPreview = (row) => {
    previewArticle.value = { ...row }
    visiblePreviewDrawer.value = true
}

// 关闭预览抽屉
const closePreview = () => {
    visiblePreviewDrawer.value = false
}

// 进入完整编辑
const goToFullEdit = () => {
    // TODO: 跳转到完整编辑页面或打开编辑抽屉
    visiblePreviewDrawer.value = false
    visibleDrawer.value = true
    updateArticle(previewArticle.value)
    isEdit.value = true
}

// 快捷操作：复制链接
const copyLink = () => {
    // TODO: 复制文章链接到剪贴板
}

// 快捷操作：取消发布
const unpublishArticle = () => {
    // TODO: 取消发布接口调用
}

const deleteCategory = (row) => {
    ElMessageBox.confirm('此操作将永久删除该文章, 是否继续?', '温馨提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    }).then(async() => {
        // 调用接口函数
        let result = await articleDeleteService(row.id)
        ElMessage({
            type: 'success',
            message: '删除成功',
        })
        // 刷新列表
        getArticleList()
    }).catch(() => {
        ElMessage({
            type: 'info',
            message: '已取消删除',
        })
    });
}

</script>
<template>
    <el-card class="page-container">
        <template #header>
            <div class="header">
                <span>文章管理</span>
                <div class="extra">
                    <el-button type="primary" @click="goToAddArticle">添加文章</el-button>
                </div>
            </div>
        </template>
        <!-- 搜索表单 -->
        <el-form inline>
            <el-form-item label="文章分类：">
                <el-select placeholder="请选择" v-model="categoryId">
                    <el-option v-for="c in categorys" :key="c.id" :label="c.categoryName" :value="c.id">
                    </el-option>
                </el-select>
            </el-form-item>

            <el-form-item label="发布状态：">
                <el-select placeholder="请选择" v-model="state">
                    <el-option label="已发布" value="已发布"></el-option>
                    <el-option label="草稿" value="草稿"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" @click="getArticleList()">搜索</el-button>
                <el-button @click="categoryId = ''; state = ''">重置</el-button>
            </el-form-item>
        </el-form>
        <!-- 文章列表 -->
        <el-table :data="articles" style="width: 100%">
            <el-table-column label="文章标题" width="400" prop="title"></el-table-column>
            <el-table-column label="分类" prop="categoryName"></el-table-column>
            <el-table-column label="发表时间" prop="createTime"> </el-table-column>
            <el-table-column label="状态" prop="state"></el-table-column>
            <el-table-column label="操作" width="100">
                <template #default="{ row }">
                    <el-button :icon="Edit" circle plain type="primary" @click="openPreview(row)"></el-button>
                    <el-button :icon="Delete" circle plain type="danger" @click="deleteCategory(row)"></el-button>
                </template>
            </el-table-column>
            <template #empty>
                <el-empty description="没有数据" />
            </template>
        </el-table>
        <!-- 分页条 -->
        <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :page-sizes="[3, 5, 10, 15]"
            layout="jumper, total, sizes, prev, pager, next" background :total="total" @size-change="onSizeChange"
            @current-change="onCurrentChange" style="margin-top: 20px; justify-content: flex-end" />
        <!-- 预览抽屉 -->
        <el-drawer v-model="visiblePreviewDrawer" title="文章快速预览" direction="rtl" size="480px" class="preview-drawer">
            <div class="preview-content">
                <!-- 封面大图 -->
                <div class="preview-cover">
                    <img :src="previewArticle.coverImg || 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800'" alt="文章封面" />
                </div>

                <!-- 标题区域 -->
                <div class="preview-title-section">
                    <h2 class="preview-title">{{ previewArticle.title }}</h2>
                    <div class="preview-tags">
                        <span class="tag-category">{{ previewArticle.categoryName }}</span>
                        <span class="tag-status" :class="previewArticle.state === '已发布' ? 'tag-published' : 'tag-draft'">
                            {{ previewArticle.state }}
                        </span>
                    </div>
                </div>

                <!-- 元信息 -->
                <div class="preview-meta">
                    <span>发布时间 {{ previewArticle.createTime }}</span>
                    <span>字数 1,286</span>
                </div>

                <!-- 分割线 -->
                <el-divider />

                <!-- 内容摘要 -->
                <div class="preview-summary">
                    <h3 class="summary-title">内容摘要</h3>
                    <p class="summary-text">
                        {{ previewArticle.content || '本攻略为长沙三日深度旅游行程规划，涵盖岳麓山、橘子洲、湖南博物院等必打卡景点，同时推荐地道美食与特色街区。行程安排兼顾人文历史、自然风光与城市体验，适合首次到访或想要深度探索长沙的游客。附交通建议、住宿区域推荐与实用贴士，助你轻松玩转星城长沙。' }}
                    </p>
                </div>

                <!-- 快速操作 -->
                <div class="preview-actions">
                    <h3 class="actions-title">快速操作</h3>
                    <div class="actions-list">
                        <el-link :underline="false" @click="copyLink">
                            <el-icon><Link /></el-icon>
                            <span>复制链接</span>
                        </el-link>
                        <el-link :underline="false" @click="unpublishArticle">
                            <el-icon><Clock /></el-icon>
                            <span>取消发布</span>
                        </el-link>
                        <el-link :underline="false" class="action-danger" @click="deleteCategory(previewArticle); closePreview()">
                            <el-icon><CircleClose /></el-icon>
                            <span>删除文章</span>
                        </el-link>
                    </div>
                </div>
            </div>

            <!-- 底部按钮 -->
            <template #footer>
                <div class="preview-footer">
                    <el-button class="btn-close-preview" @click="closePreview">关闭</el-button>
                    <el-button type="primary" class="btn-full-edit" @click="goToFullEdit">进入完整编辑</el-button>
                </div>
            </template>
        </el-drawer>

        <!-- 添加/编辑文章抽屉 -->
        <el-drawer v-model="visibleDrawer" title="添加文章" direction="rtl" size="50%">
            <!-- 添加文章表单 -->
            <el-form :model="articleModel" label-width="100px">
                <el-form-item label="文章标题">
                    <el-input v-model="articleModel.title" placeholder="请输入标题"></el-input>
                </el-form-item>
                <el-form-item label="文章分类">
                    <el-select placeholder="请选择" v-model="articleModel.categoryId">
                        <el-option v-for="c in categorys" :key="c.id" :label="c.categoryName" :value="c.id">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="文章封面">
                    <!-- 
                        auto-upload: false 禁止自动上传
                        action: 设置服务器接口路径
                        name: 设置上传的文件字段名
                        headers: 设置请求头
                        on-success: 上传成功后的回调函数
                    --->
                    <el-upload class="avatar-uploader" :auto-upload="true" :show-file-list="false" action="/api/upload"
                        name="file" :headers="{ 'Authorization': tokenStore.token }" :on-success="uploadSuccess">
                        <img v-if="articleModel.coverImg" :src="articleModel.coverImg" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
                <el-form-item label="文章内容">
                    <div class="editor">
                        <quill-editor :key="editorKey" theme="snow" v-model:content="articleModel.content" contentType="html">
                        </quill-editor>
                    </div>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="addArticle('已发布')">发布</el-button>
                    <el-button type="info" @click="addArticle('草稿')">草稿</el-button>
                </el-form-item>
            </el-form>
        </el-drawer>
    </el-card>
</template>
<style lang="scss" scoped>
.page-container {
    min-height: 100%;
    box-sizing: border-box;

    .header {
        display: flex;
        align-items: center;
        justify-content: space-between;
    }
}

.avatar-uploader {
    :deep() {
        .avatar {
            width: 178px;
            height: 178px;
            display: block;
        }

        .el-upload {
            border: 1px dashed var(--el-border-color);
            border-radius: 6px;
            cursor: pointer;
            position: relative;
            overflow: hidden;
            transition: var(--el-transition-duration-fast);
        }

        .el-upload:hover {
            border-color: var(--el-color-primary);
        }

        .el-icon.avatar-uploader-icon {
            font-size: 28px;
            color: #8c939d;
            width: 178px;
            height: 178px;
            text-align: center;
        }
    }
}

.editor {
    width: 100%;

    :deep(.ql-editor) {
        min-height: 200px;
    }
}

// ========== 预览抽屉样式 ==========
.preview-drawer {
    :deep(.el-drawer__header) {
        margin-bottom: 0;
        padding-bottom: 16px;
        border-bottom: 1px solid #e4e7ed;
    }

    :deep(.el-drawer__body) {
        padding: 0;
        overflow-y: auto;
    }

    :deep(.el-drawer__footer) {
        padding: 16px 20px;
        border-top: 1px solid #e4e7ed;
    }
}

.preview-content {
    padding: 20px;

    .preview-cover {
        width: 100%;
        height: 200px;
        border-radius: 8px;
        overflow: hidden;
        margin-bottom: 20px;

        img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .preview-title-section {
        margin-bottom: 12px;

        .preview-title {
            font-size: 20px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 12px 0;
            line-height: 1.4;
        }

        .preview-tags {
            display: flex;
            gap: 8px;

            .tag-category {
                display: inline-block;
                padding: 2px 10px;
                font-size: 12px;
                color: #1677ff;
                background: #f0f5ff;
                border-radius: 4px;
                border: 1px solid #d4e8ff;
            }

            .tag-published {
                display: inline-block;
                padding: 2px 10px;
                font-size: 12px;
                color: #67c23a;
                background: #f0f9eb;
                border-radius: 4px;
                border: 1px solid #c2e7b0;
            }

            .tag-draft {
                display: inline-block;
                padding: 2px 10px;
                font-size: 12px;
                color: #909399;
                background: #f4f4f5;
                border-radius: 4px;
                border: 1px solid #dcdfe6;
            }
        }
    }

    .preview-meta {
        display: flex;
        justify-content: space-between;
        font-size: 13px;
        color: #666;
        margin-bottom: 8px;
    }

    .preview-summary {
        margin-bottom: 24px;

        .summary-title {
            font-size: 15px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 12px 0;
        }

        .summary-text {
            font-size: 13px;
            color: #606266;
            line-height: 1.8;
            margin: 0;
        }
    }

    .preview-actions {
        .actions-title {
            font-size: 15px;
            font-weight: 600;
            color: #303133;
            margin: 0 0 12px 0;
        }

        .actions-list {
            display: flex;
            gap: 24px;

            .el-link {
                display: flex;
                align-items: center;
                gap: 4px;
                font-size: 13px;
                color: #606266;

                .el-icon {
                    font-size: 15px;
                }

                &:hover {
                    color: #1677ff;
                }

                &.action-danger:hover {
                    color: #f56c6c;
                }
            }
        }
    }
}

.preview-footer {
    display: flex;
    gap: 12px;
    width: 100%;

    .btn-close-preview {
        flex: 1;
        border-color: #dcdfe6;
        color: #606266;
        border-radius: 6px;
        height: 40px;
        font-size: 14px;

        &:hover {
            color: #1677ff;
            border-color: #1677ff;
        }
    }

    .btn-full-edit {
        flex: 1;
        background: #1677ff;
        border-color: #1677ff;
        border-radius: 6px;
        height: 40px;
        font-size: 14px;

        &:hover {
            background: #4096ff;
            border-color: #4096ff;
        }
    }
}
</style>