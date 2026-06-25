d:\code\big-event\src\views\article\ArticleEditor.vue
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useTokenStore } from '@/stores/token.js'
import { ElMessage } from 'element-plus'
import { articleAddService } from '@/api/article.js'
import { aiGenerateService } from '@/api/ai.js'
const router = useRouter()
const tokenStore = useTokenStore()
import {
    ArrowLeft,
    Star,
    Check,
    Plus,
    Upload,
    Select,
    Edit,
    Refresh,
    Document,
    Warning,
    Expand,
    Right,
    Finished,
    Timer,
    Search,
    Guide,
    Loading,
    Reading,
    Opportunity,
    Promotion,        // ← 新增：纸飞机
    MagicStick,       // ← 新增：魔法棒
    ChatDotRound,     // ← 新增：聊天圆点
    User,             // ← 新增：用户
    Clock,            // ← 新增：时钟
    Delete,           // ← 新增：删除
    EditPen           // ← 新增：编辑笔
} from '@element-plus/icons-vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

// ---------- 左侧编辑区数据 ----------

// 文章表单数据模型
const articleForm = ref({
    title: '长沙三日深度旅游攻略',
    category: 4,
    coverImg: 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800',
    content: ''
})

// 文章分类选项
const categoryOptions = ref([
    { value: 1, label: '美食' },
    { value: 2, label: '娱乐' },
    { value: 3, label: '军事' },
    { value: 4, label: '旅游' },
    { value: 5, label: '科技' },
    { value: 6, label: '历史' }
])

// 文章内容预填充文本（攻略正文）
const defaultContent = ref(
    '<h2>长沙三日深度旅游攻略</h2>' +
    '<p><strong>Day 1：岳麓山 — 岳麓书院 — 橘子洲头</strong></p>' +
    '<p>上午抵达长沙后，首先前往<strong>岳麓山</strong>。这里是中国四大赏枫胜地之一，也是长沙的标志性景点。乘坐索道上山，可以俯瞰整个长沙市区和湘江美景。山上有爱晚亭、麓山寺等著名景点，建议游玩时间2-3小时。</p>' +
    '<p>中午下山后，直接前往山脚下的<strong>岳麓书院</strong>。作为中国历史上赫赫闻名的四大书院之一，这里保存着大量珍贵的碑匾文物。漫步在千年学府中，感受浓厚的文化氛围。</p>' +
    '<p>下午前往<strong>橘子洲头</strong>，乘坐观光车游览整个橘子洲景区。在洲头广场瞻仰毛泽东青年雕像，欣赏湘江两岸风光。傍晚时分，夕阳下的橘子洲格外美丽，是不可错过的拍照时机。</p>' +
    '<p><strong>Day 2：太平老街 — 坡子街 — 火宫殿 — 天心阁</strong></p>' +
    '<p>第二天开启美食探索之旅。上午前往<strong>太平老街</strong>，这里保留着明清时期的街巷格局，青石板路两旁是琳琅满目的特色小吃店和文创店铺。推荐品尝：黑色经典臭豆腐、糖油粑粑、刮凉粉。</p>' +
    '<p>接着步行至<strong>坡子街</strong>和<strong>火宫殿</strong>。坡子街是长沙最繁华的美食街之一，火宫殿则以臭豆腐和姊妹团子闻名。午餐可以在火宫殿品尝正宗湘菜，剁椒鱼头、辣椒炒肉都是必点菜。</p>' +
    '<p>下午游览<strong>天心阁</strong>，这是长沙仅存的古城标志。登阁远眺，长沙城景尽收眼底。阁内的古城墙遗址和历史文化展览也值得一看。</p>' +
    '<p><strong>Day 3：湖南省博物馆 — 马王堆汉墓 — IFO国金中心</strong></p>' +
    '<p>上午参观<strong>湖南省博物馆</strong>，这里是了解湖湘文化的最佳去处。镇馆之宝马王堆汉墓出土文物举世闻名，辛追夫人千年不腐的遗体、素纱禅衣等珍贵文物令人叹为观止。建议提前预约，预留2-3小时参观时间。</p>' +
    '<p>中午在博物馆附近的<strong>湘春酒家</strong>用餐，品尝地道湘菜。</p>' +
    '<p>下午前往长沙最繁华的商业中心<strong>IFO国金中心</strong>，这里是长沙新地标。可以逛逛各大品牌店，也可以去顶楼观景台俯瞰长沙城市全景。晚上在商场内选择一家口碑餐厅，为旅程画上完美句号。</p>' +
    '<p><strong>【小贴士】</strong></p>' +
    '<ul><li>最佳旅行时间：每年3-5月或9-11月，气候宜人</li><li>交通：长沙地铁覆盖主要景点，建议购买地铁一日票</li><li>住宿：推荐住在五一广场附近，交通便利，美食集中</li><li>美食推荐：茶颜悦色、超级文和友、一盏灯、炊烟时代</li></ul>'
)

// 文章内容编辑器引用
const editorRef = ref(null)
const editorKey = ref(0)

// 已自动保存标记
const isAutoSaved = ref(true)

// 文章字数统计
const wordCount = ref(0)

// 编辑器内容变化回调
const onContentChange = () => {
    isAutoSaved.value = true
    // 计算字数（去除HTML标签）
    if (articleForm.value.content) {
        const text = articleForm.value.content.replace(/<[^>]+>/g, '')
        wordCount.value = text.length
    }
}

// 保存草稿
const saveDraft = async () => {
    const postData = {
        title: articleForm.value.title,
        categoryId: articleForm.value.category,
        coverImg: articleForm.value.coverImg,
        content: articleForm.value.content,
        state: '草稿'
    }
    let res = await articleAddService(postData)
    ElMessage.success(res.message ? res.message : '草稿保存成功')
    isAutoSaved.value = true
}

// 发布文章
const publishArticle = async () => {
    const postData = {
        title: articleForm.value.title,
        categoryId: articleForm.value.category,
        coverImg: articleForm.value.coverImg,
        content: articleForm.value.content,
        state: '已发布'
    }
    let res = await articleAddService(postData)
    ElMessage.success(res.message ? res.message : '文章发布成功')
    // 发布成功后跳转回文章管理页面
    router.push('/article/manage')
}

// 更换封面 - 隐藏的上传组件引用
const coverUploadRef = ref(null)

// 封面上传成功回调
const uploadCoverSuccess = (response) => {
    articleForm.value.coverImg = response.data
}

// 触发封面上传选择
const changeCover = () => {
    coverUploadRef.value?.$el.querySelector('input').click()
}

// 返回上一页
const goBack = () => {
    router.push('/article/manage')
}


// ---------- 右侧AI助手面板数据 ----------

// 页面加载时自动填充默认内容
onMounted(() => {
    articleForm.value.content = defaultContent.value
    onContentChange()
})

// ---------- 右侧AI助手面板数据（聊天式） ----------

// 聊天消息列表
const messages = ref([])

// 输入框内容
const aiInput = ref('')

// 加载状态
const aiLoading = ref(false)

// 当前正在处理的action
const currentAction = ref('')

// Action 映射表
const ACTION_MAP = {
    '生成标题': 'GENERATE_TITLE',
    '推荐分类': 'RECOMMEND_CATEGORY',
    '润色全文': 'POLISH',
    '续写内容': 'CONTINUE',
    '扩写选中': 'EXPAND',
    '检查错别字': 'PROOFREAD'
}

// 快捷操作按钮
const quickActions = ['生成标题', '推荐分类', '润色全文', '续写内容', '扩写选中', '检查错别字']

// 输入字数统计
const aiInputCount = ref(0)
const onAiInputChange = () => { aiInputCount.value = aiInput.value.length }

// 聊天容器ref
const chatContainerRef = ref(null)

// ========== 聊天核心方法 ==========

// 发送消息（快捷按钮或自定义输入）
const sendMessage = async (actionLabel = null, customText = null) => {
    const text = customText || aiInput.value.trim()
    if (!text && !actionLabel) return

    const isCustom = !actionLabel
    const action = isCustom ? 'CUSTOM' : ACTION_MAP[actionLabel]
    currentAction.value = action

    // 添加用户消息
    messages.value.push({
        id: Date.now(), role: 'user', content: text,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })

    if (!customText) aiInput.value = ''
    aiLoading.value = true

    // 构建请求参数
    let selectedText = ''
    try {
        if (editorRef.value) {
            const quill = editorRef.value.getQuill()
            const selection = quill.getSelection(true)
            if (selection && selection.length > 0) {
                selectedText = quill.getText(selection.index, selection.length).trim()
            }
        }
    } catch (e) {}

    const isExpandOrProofread = (action === 'EXPAND' || action === 'PROOFREAD')
    const payload = {
        action, target: isExpandOrProofread ? (selectedText ? 'SELECTION' : 'FULL_CONTENT') : 'FULL_CONTENT',
        title: articleForm.value.title, categoryId: articleForm.value.category || null,
        content: articleForm.value.content, selectedText: selectedText || '', instruction: isCustom ? text : ''
    }

    try {
        let res = await aiGenerateService(payload)
        messages.value.push(buildAIMessage(action, res.data.output))
    } catch (error) {
        messages.value.push({
            id: Date.now() + 1, role: 'ai',
            content: error.response?.data?.message || 'AI服务暂时不可用，请稍后重试', type: 'error',
            time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
        })
    } finally {
        aiLoading.value = false
        setTimeout(() => scrollToBottom(), 100)
    }
}

// 构建AI回复消息对象
const buildAIMessage = (action, output) => {
    const base = { id: Date.now() + 1, role: 'ai', time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }
    switch (action) {
        case 'GENERATE_TITLE': return { ...base, type: 'titles', content: '', data: output.titles || [] }
        case 'RECOMMEND_CATEGORY': return { ...base, type: 'category', content: output.category?.name || '', data: output.category }
        case 'PROOFREAD': return { ...base, type: 'corrections', content: output.content || '', data: output.corrections || [] }
        default: return { ...base, type: 'content', content: output.content || '', data: output.content || '' }
    }
}

const handleQuickAction = (label) => sendMessage(label)
const handleSend = () => { if (!aiInput.value.trim()) return; sendMessage(null) }
const handleKeydown = (e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend() } }

// ========== AI结果操作方法 ==========

const replaceToContent = (msg) => {
    if (!msg.data) return
    if (currentAction.value === 'CONTINUE') { articleForm.value.content += msg.data }
    else { articleForm.value.content = typeof msg.data === 'string' ? msg.data : '' }
    onContentChange()
    ElMessage.success('已替换到正文')
}

const insertAtCursor = (msg) => {
    if (!msg.data || !editorRef.value) return
    try {
        const quill = editorRef.value.getQuill()
        const index = quill.getSelection()?.index ?? quill.getLength()
        quill.clipboard.dangerouslyPasteHTML(index, typeof msg.data === 'string' ? msg.data : '')
        onContentChange()
        ElMessage.success('已插入到光标位置')
    } catch (e) { ElMessage.error('插入失败') }
}

const copyContent = async (msg) => {
    const text = typeof msg.data === 'string' ? msg.data : JSON.stringify(msg.data, null, 2)
    try { await navigator.clipboard.writeText(text); ElMessage.success('已复制到剪贴板') }
    catch (e) { ElMessage.error('复制失败') }
}

const adoptTitle = (title) => { articleForm.value.title = title; ElMessage.success(`已采用标题：${title}`) }
const adoptCategoryFromChat = (cat) => { articleForm.value.category = cat.id; ElMessage.success(`已采用分类：${cat.name}`) }

const regenerateLast = () => {
    const lastUserMsg = [...messages.value].reverse().find(m => m.role === 'user')
    if (lastUserMsg) {
        const lastIndex = messages.value.findLastIndex(m => m.role === 'ai')
        if (lastIndex > -1) messages.value.splice(lastIndex, 1)
        sendMessage(null, lastUserMsg.content)
    }
}

const scrollToBottom = () => {
    if (chatContainerRef.value) chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
}
</script>

<template>
    <div class="article-editor">
        <!-- ==================== 左侧：文章编辑区 ==================== -->
        <div class="editor-main">
            <!-- 顶部操作栏 -->
            <div class="editor-header">
                <div class="header-left">
                    <el-link :underline="false" @click="goBack" class="back-link">
                        <el-icon><ArrowLeft /></el-icon>
                        <span>返回</span>
                    </el-link>
                    <h1 class="page-title">编辑文章</h1>
                </div>
                <div class="header-right">
                    <el-button class="btn-draft" @click="saveDraft">保存草稿</el-button>
                    <el-button type="primary" class="btn-publish" @click="publishArticle">
                        <el-icon><Finished /></el-icon>
                        <span>发布文章</span>
                    </el-button>
                </div>
            </div>

            <!-- 表单卡片 -->
            <div class="editor-card">
                <!-- 文章标题 -->
                <div class="form-group">
                    <label class="form-label">文章标题</label>
                    <el-input
                        v-model="articleForm.title"
                        placeholder="请输入文章标题"
                        class="form-input"
                    />
                </div>

                <!-- 文章分类 -->
                <div class="form-group">
                    <label class="form-label">文章分类</label>
                    <el-select
                        v-model="articleForm.category"
                        placeholder="请选择分类"
                        class="form-select"
                    >
                        <el-option
                            v-for="item in categoryOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                </div>

                <!-- 文章封面 -->
                <div class="form-group">
                    <label class="form-label">文章封面</label>
                    <div class="cover-section">
                    <div class="cover-preview">
                        <img :src="articleForm.coverImg" alt="文章封面" />
                    </div>
                    <!-- 隐藏的上传组件 -->
                    <el-upload
                        ref="coverUploadRef"
                        class="hidden-upload"
                        :auto-upload="true"
                        :show-file-list="false"
                        action="/api/upload"
                        name="file"
                        :headers="{ 'Authorization': tokenStore.token }"
                        accept="image/*"
                        :on-success="uploadCoverSuccess"
                    />
                    <el-button class="btn-change-cover" @click="changeCover">
                        <el-icon><Upload /></el-icon>
                        <span>更换封面</span>
                    </el-button>
                </div>
                </div>

                <!-- 文章内容（富文本编辑器） -->
                <div class="form-group">
                    <label class="form-label">文章内容</label>
                    <div class="editor-wrapper">
                        <QuillEditor
                            ref="editorRef"
                            :key="editorKey"
                            v-model:content="articleForm.content"
                            contentType="html"
                            theme="snow"
                            toolbar="full"
                            @update:content="onContentChange"
                            class="quill-editor"
                        />
                    </div>
                    <!-- 编辑器底部状态栏 -->
                    <div class="editor-footer">
                        <span class="auto-save">
                            <el-icon class="save-icon"><Check /></el-icon>
                            已自动保存
                        </span>
                        <span class="word-count">字数：{{ wordCount }}</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- ==================== 右侧：AI写作助手面板（聊天式） ==================== -->
        <div class="ai-panel">
            <div class="ai-chat-container">
                <!-- 顶部标题栏 -->
                <div class="chat-header">
                    <div class="chat-title-area">
                        <el-icon class="sparkle-icon"><MagicStick /></el-icon>
                        <span class="chat-title">AI 写作助手</span>
                    </div>
                    <div class="chat-header-actions">
                        <el-icon class="header-action-icon"><Clock /></el-icon>
                        <el-icon class="header-action-icon"><Delete /></el-icon>
                    </div>
                </div>

                <!-- 聊天消息区域 -->
                <div class="chat-messages" ref="chatContainerRef">
                    <!-- 欢迎消息 -->
                    <div class="message-row ai-msg">
                        <div class="avatar ai-avatar">
                            <span class="ai-avatar-icon">🤖</span>
                        </div>
                        <div class="message-bubble ai-bubble">
                            <p>你好！我是你的 AI 写作助手，</p>
                            <p>可以帮你润色文章、生成标题、推荐分类等。</p>
                            <p>有什么需要我帮忙的吗？</p>
                        </div>
                    </div>

                    <!-- 用户消息示例 -->
                    <template v-for="msg in messages" :key="msg.id">
                        <!-- 用户消息 -->
                        <div v-if="msg.role === 'user'" class="message-row user-msg">
                            <div class="message-bubble user-bubble">{{ msg.content }}</div>
                            <div class="avatar user-avatar">
                                <span class="user-avatar-icon">😊</span>
                            </div>
                        </div>
                        <!-- AI回复消息 -->
                        <div v-else class="message-row ai-msg">
                            <div class="avatar ai-avatar">
                                <span class="ai-avatar-icon">🤖</span>
                            </div>
                            <div class="message-bubble ai-bubble">
                                <!-- 文本类回复 -->
                                <template v-if="msg.type === 'content' || msg.type === 'error'">
                                    <div v-if="msg.type === 'error'" class="error-text">{{ msg.content }}</div>
                                    <div v-else class="ai-content-text" v-html="msg.content"></div>
                                    <div class="msg-actions">
                                        <button class="msg-btn" @click="replaceToContent(msg)">替换到正文</button>
                                        <button class="msg-btn" @click="insertAtCursor(msg)">插入光标处</button>
                                        <button class="msg-btn" @click="copyContent(msg)">复制</button>
                                        <button class="msg-btn" @click="regenerateLast()">重新生成</button>
                                    </div>
                                </template>
                                <!-- 标题列表回复 -->
                                <template v-else-if="msg.type === 'titles'">
                                    <p class="ai-content-intro">好的，已为你生成以下标题：</p>
                                    <div class="title-list">
                                        <div
                                            v-for="(title, idx) in msg.data"
                                            :key="idx"
                                            class="title-item"
                                            @click="adoptTitle(title)"
                                        >{{ idx + 1 }}. {{ title }}</div>
                                    </div>
                                </template>
                                <!-- 分类推荐回复 -->
                                <template v-else-if="msg.type === 'category'">
                                    <p class="ai-content-intro">根据文章内容，建议分类：</p>
                                    <div class="category-result">
                                        <strong>{{ msg.data?.name }}</strong>
                                        <span class="cat-reason">{{ msg.data?.reason }}</span>
                                    </div>
                                    <div class="msg-actions">
                                        <button class="msg-btn primary" @click="adoptCategoryFromChat(msg.data)">采用此分类</button>
                                    </div>
                                </template>
                                <!-- 纠错结果回复 -->
                                <template v-else-if="msg.type === 'corrections'">
                                    <p class="ai-content-intro">发现 {{ msg.data?.length || 0 }} 处问题：</p>
                                    <div class="correction-list">
                                        <div v-for="(c, i) in msg.data" :key="i" class="correction-item">
                                            <del>{{ c.original }}</del>
                                            <span class="arrow">→</span>
                                            <span class="corrected">{{ c.replacement }}</span>
                                            <span class="reason">({{ c.reason }})</span>
                                        </div>
                                    </div>
                                    <div class="msg-actions">
                                        <button class="msg-btn primary" @click="replaceToContent(msg)">采用修正结果</button>
                                        <button class="msg-btn" @click="copyContent(msg)">复制结果</button>
                                    </div>
                                </template>
                            </div>
                        </div>
                    </template>

                    <!-- 加载中状态 -->
                    <div v-if="aiLoading" class="message-row ai-msg">
                        <div class="avatar ai-avatar loading">
                            <span class="ai-avatar-icon thinking">🤔</span>
                        </div>
                        <div class="message-bubble ai-bubble loading-bubble">
                            <span class="typing-dots"><i></i><i></i><i></i></span>
                        </div>
                    </div>
                </div>

                <!-- 快捷操作标签 -->
                <div class="quick-tags">
                    <button
                        v-for="tag in quickActions"
                        :key="tag"
                        class="quick-tag"
                        :disabled="aiLoading"
                        @click="handleQuickAction(tag)"
                    >
                        <el-icon v-if="tag === '生成标题'"><Select /></el-icon>
                        <el-icon v-else-if="tag === '推荐分类'"><Guide /></el-icon>
                        <el-icon v-else-if="tag === '润色全文'"><EditPen /></el-icon>
                        <el-icon v-else-if="tag === '续写内容'"><Right /></el-icon>
                        <el-icon v-else-if="tag === '扩写选中'"><Expand /></el-icon>
                        <el-icon v-else><Search /></el-icon>
                        {{ tag }}
                    </button>
                </div>

                <!-- 底部输入区域 -->
                <div class="chat-input-area">
                    <el-input
                        v-model="aiInput"
                        type="textarea"
                        :rows="2"
                        placeholder="请输入你的需求..."
                        resize="none"
                        @input="onAiInputChange"
                        @keydown="handleKeydown"
                        class="chat-input"
                    />
                    <button 
                        class="send-btn" 
                        :class="{ active: aiInput.trim() }" 
                        :disabled="!aiInput.trim() || aiLoading"
                        @click="handleSend"
                    >
                        <el-icon><Promotion /></el-icon>
                    </button>
                </div>
            </div>
        </div>
    </div>
</template>

<style lang="scss" scoped>
// ========== 全局变量 ==========
$primary: #1677ff;
$bg-page: #f7f8fa;
$bg-card: #fff;
$text-primary: #303133;
$text-secondary: #666;
$border-color: #e4e7ed;
$border-radius: 6px;
$shadow-card: 0 2px 12px rgba(0, 0, 0, 0.06);

// ========== 页面容器 ==========
.article-editor {
    display: flex;
    gap: 24px;
    height: calc(100vh - 120px);
    padding: 0;
    background: $bg-page;
}

// ========== 左侧编辑区 ==========
.editor-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
    padding-bottom: 20px;

    // 顶部操作栏
    .editor-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 16px;
        flex-shrink: 0;

        .header-left {
            display: flex;
            align-items: center;
            gap: 16px;

            .back-link {
                font-size: 14px;
                color: $text-secondary;
                display: flex;
                align-items: center;
                gap: 4px;
                white-space: nowrap;

                &:hover {
                    color: $primary;
                }
            }

            .page-title {
                font-size: 20px;
                font-weight: 600;
                color: $text-primary;
                margin: 0;
                white-space: nowrap;
            }
        }

        .header-right {
            display: flex;
            align-items: center;
            gap: 12px;
            flex-shrink: 0;

            .btn-draft {
                border: 1px solid $border-color;
                color: $text-primary;
                background: #fff;
                border-radius: $border-radius;
                padding: 8px 20px;
                font-size: 14px;

                &:hover {
                    color: $primary;
                    border-color: $primary;
                }
            }

            .btn-publish {
                background: $primary;
                border-color: $primary;
                border-radius: $border-radius;
                padding: 8px 20px;
                font-size: 14px;

                &:hover {
                    background: #4096ff;
                    border-color: #4096ff;
                }
            }
        }
    }

    // 表单卡片
    .editor-card {
        background: $bg-card;
        border-radius: 8px;
        padding: 32px 36px;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
        flex: 1;
        overflow-y: auto;
    }

    .form-group {
        margin-bottom: 24px;

        .form-label {
            display: block;
            font-size: 14px;
            font-weight: 500;
            color: $text-primary;
            margin-bottom: 8px;
        }

        .form-input {
            :deep(.el-input__wrapper) {
                border-radius: 6px;
                box-shadow: 0 0 0 1px $border-color inset;
                transition: all 0.25s ease;

                &:hover {
                    box-shadow: 0 0 0 1px #b3d8ff inset;
                    border-color: transparent;
                }

                &:focus {
                    box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.15) inset !important;
                    border-color: $primary !important;
                }
            }
        }

        .form-select {
            width: 260px;

            :deep(.el-input__wrapper) {
                border-radius: 6px;
                box-shadow: 0 0 0 1px $border-color inset;
                transition: all 0.25s ease;
            }
        }

        // 封面区域
        .cover-section {
            display: flex;
            align-items: flex-start;
            gap: 16px;

            .cover-preview {
                width: 220px;
                height: 140px;
                border-radius: 8px;
                overflow: hidden;
                border: 1px solid $border-color;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
                transition: transform 0.3s ease, box-shadow 0.3s ease;

                &:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
                }

                img {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }

            .btn-change-cover {
                border: 1px solid $border-color;
                color: $text-primary;
                background: #fff;
                border-radius: 8px;
                font-size: 13px;
                padding: 10px 20px;
                align-self: flex-end;
                transition: all 0.25s ease;

                &:hover {
                    color: #fff;
                    background: $primary;
                    border-color: $primary;
                    transform: translateY(-1px);
                    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.3);
                }
            }
        }

        // 富文本编辑器
        .editor-wrapper {
            border: 1px solid $border-color;
            border-radius: 8px;
            overflow: hidden;
            transition: box-shadow 0.25s ease;

            &:hover {
                box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
            }

            .quill-editor {
                :deep(.ql-toolbar) {
                    border: none;
                    border-bottom: 1px solid $border-color;
                    background: linear-gradient(to bottom, #fafafa, #f5f5f5);
                }

                :deep(.ql-container) {
                    border: none;
                    min-height: 380px;
                    font-size: 14px;
                    line-height: 1.7;
                }
            }
        }

        // 编辑器底部状态栏
        .editor-footer {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 10px 0 0;
            font-size: 13px;

            .auto-save {
                display: flex;
                align-items: center;
                gap: 4px;
                color: #67c23a;

                .save-icon {
                    font-size: 16px;
                }
            }

            .word-count {
                color: $text-secondary;
            }
        }
    }
}

// ========== 右侧AI助手面板（聊天式） ==========
.ai-panel {
    width: 360px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    background: #fff;
    border-left: 1px solid #e8e8e8;
    height: calc(100vh - 120px);

    .ai-chat-container {
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    // 顶部标题栏
    .chat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 16px 20px;
        border-bottom: 1px solid #f0f0f0;
        background: #fff;

        .chat-title-area {
            display: flex;
            align-items: center;
            gap: 8px;

            .sparkle-icon {
                font-size: 18px;
                color: $primary;
            }

            .chat-title {
                font-size: 15px;
                font-weight: 600;
                color: $text-primary;
            }
        }

        .chat-header-actions {
            display: flex;
            gap: 12px;

            .header-action-icon {
                font-size: 16px;
                color: #999;
                cursor: pointer;
                transition: color 0.2s;

                &:hover { color: $text-primary; }
            }
        }
    }

    // 聊天消息区域
    .chat-messages {
        flex: 1;
        overflow-y: auto;
        padding: 16px 16px 8px;
        background: #fafbfc;

        .message-row {
            display: flex;
            gap: 10px;
            margin-bottom: 16px;

            &.user-msg {
                justify-content: flex-end;
            }

            &.ai-msg {
                justify-content: flex-start;
            }
        }

        // 头像
        .avatar {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
            flex-shrink: 0;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            transition: transform 0.2s ease;

            &:hover { transform: scale(1.1); }

            &.ai-avatar {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                
                .ai-avatar-icon {
                    font-size: 20px;
                    filter: drop-shadow(0 1px 2px rgba(0,0,0,0.2));
                    
                    &.thinking {
                        animation: thinking-bounce 1.5s ease-in-out infinite;
                    }
                }

                &.loading { 
                    background: linear-gradient(135deg, #b8c6db 0%, #f5f7fa 100%);
                    animation: pulse-loading 2s ease-in-out infinite;
                }
            }

            &.user-avatar {
                background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
                
                .user-avatar-icon {
                    font-size: 19px;
                    filter: drop-shadow(0 1px 2px rgba(0,0,0,0.15));
                }
            }
        }

        // 消息气泡
        .message-bubble {
            max-width: 280px;
            padding: 10px 14px;
            border-radius: 12px;
            font-size: 13px;
            line-height: 1.6;
            word-break: break-word;

            p { margin: 4px 0; &:first-child { margin-top: 0; } &:last-child { margin-bottom: 0; } }

            &.user-bubble {
                background: linear-gradient(135deg, #1677ff, #4096ff);
                color: #fff;
                border-bottom-right-radius: 4px;
            }

            &.ai-bubble {
                background: #fff;
                color: $text-primary;
                border: 1px solid #eee;
                border-bottom-left-radius: 4px;
                box-shadow: 0 1px 3px rgba(0,0,0,0.04);

                &.loading-bubble {
                    padding: 14px 20px;
                }
            }
        }

        // AI内容文本
        .ai-content-text {
            :deep(p) { margin: 6px 0; line-height: 1.7; }
        }

        .ai-content-intro {
            color: $text-secondary;
            font-size: 13px;
            margin-bottom: 10px;
        }

        .error-text {
            color: #f56c6c;
        }

        // 消息操作按钮
        .msg-actions {
            display: flex;
            gap: 6px;
            margin-top: 10px;
            flex-wrap: wrap;

            .msg-btn {
                padding: 5px 12px;
                border: 1px solid #dcdfe6;
                background: #fff;
                border-radius: 14px;
                font-size: 12px;
                color: $text-secondary;
                cursor: pointer;
                transition: all 0.2s;

                &:hover {
                    color: $primary;
                    border-color: $primary;
                    background: #f0f5ff;
                }

                &.primary {
                    color: $primary;
                    border-color: $primary;
                    font-weight: 500;
                }
            }
        }

        // 标题列表
        .title-list {
            .title-item {
                padding: 8px 12px;
                margin-bottom: 6px;
                border-radius: 6px;
                background: #f8f9fa;
                cursor: pointer;
                transition: all 0.2s;
                font-size: 13px;

                &:hover {
                    background: #e6f0ff;
                    color: $primary;
                    transform: translateX(3px);
                }
            }
        }

        // 分类结果
        .category-result {
            strong { color: $primary; }
            .cat-reason {
                display: block;
                font-size: 12px;
                color: #999;
                margin-top: 4px;
            }
        }

        // 纠错列表
        .correction-list {
            .correction-item {
                padding: 6px 0;
                font-size: 13px;
                line-height: 1.5;
                border-bottom: 1px dashed #eee;

                &:last-child { border-bottom: none; }

                del { color: #f56c6c; margin-right: 4px; }
                .arrow { color: #ccc; margin: 0 4px; }
                .corrected { color: #67c23a; font-weight: 600; margin-right: 4px; }
                .reason { color: #999; font-size: 12px; }
            }
        }

        // 加载动画
        .typing-dots {
            display: inline-flex;
            gap: 4px;

            i {
                width: 6px;
                height: 6px;
                border-radius: 50%;
                background: #bbb;
                animation: typing 1.4s infinite ease-in-out;

                &:nth-child(2) { animation-delay: 0.2s; }
                &:nth-child(3) { animation-delay: 0.4s; }
            }
        }
    }

    // 快捷操作标签
    .quick-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        padding: 12px 16px;
        border-top: 1px solid #f0f0f0;
        background: #fff;

        .quick-tag {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 6px 12px;
            border: 1px solid #e0e0e0;
            background: #fafafa;
            border-radius: 16px;
            font-size: 12px;
            color: $text-secondary;
            cursor: pointer;
            transition: all 0.2s;

            .el-icon { font-size: 12px; }

            &:hover:not(:disabled) {
                color: $primary;
                border-color: $primary;
                background: #f0f5ff;
            }

            &:disabled { opacity: 0.5; cursor: not-allowed; }
        }
    }

    // 底部输入区域
    .chat-input-area {
        display: flex;
        align-items: flex-end;
        gap: 10px;
        padding: 12px 16px;
        border-top: 1px solid #f0f0f0;
        background: #fff;

        .chat-input {
            flex: 1;

            :deep(.el-textarea__inner) {
                border-radius: 8px;
                border-color: #e0e0e0;
                font-size: 13px;
                resize: none;
                padding: 10px 12px;
                line-height: 1.5;

                &:focus {
                    border-color: $primary;
                    box-shadow: 0 0 0 2px rgba(22,119,255,0.08);
                }
            }
        }

        .send-btn {
            width: 38px;
            height: 38px;
            border-radius: 50%;
            border: none;
            background: #f0f0f0;
            color: #bbb;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: not-allowed;
            transition: all 0.3s ease;
            flex-shrink: 0;

            .el-icon { 
                font-size: 18px; 
                transition: all 0.3s ease;
            }

            &.active {
                background: linear-gradient(135deg, #1677ff, #4096ff);
                color: #fff;
                cursor: pointer;
                box-shadow: 0 2px 10px rgba(22,119,255,0.35);

                .el-icon {
                    animation: send-fly 0.4s ease-out;
                }

                &:hover {
                    transform: scale(1.08);
                    box-shadow: 0 4px 16px rgba(22,119,255,0.45);
                    
                    .el-icon {
                        transform: translateX(2px) translateY(-2px);
                    }
                }

                &:active {
                    transform: scale(0.95);
                }
            }
        }
    }
}

// ========== 滚动条美化 ==========
.editor-main::-webkit-scrollbar,
.chat-messages::-webkit-scrollbar {
    width: 5px;
}

.editor-main::-webkit-scrollbar-thumb,
.chat-messages::-webkit-scrollbar-thumb {
    background: #d0d0d0;
    border-radius: 3px;

    &:hover { background: #bbb; }
}

.editor-main::-webkit-scrollbar-track,
.chat-messages::-webkit-scrollbar-track {
    background: transparent;
}

// ========== 动画 ==========
@keyframes typing {
    0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
    30% { transform: translateY(-4px); opacity: 1; }
}

@keyframes thinking-bounce {
    0%, 100% { transform: rotate(0deg); }
    25% { transform: rotate(-10deg); }
    75% { transform: rotate(10deg); }
}

@keyframes pulse-loading {
    0%, 100% { opacity: 1; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
    50% { opacity: 0.7; box-shadow: 0 2px 12px rgba(0,0,0,0.15); }
}

@keyframes send-fly {
    0% { transform: translate(0, 0) scale(1); opacity: 1; }
    30% { transform: translate(6px, -8px) scale(1.15); opacity: 1; }
    60% { transform: translate(10px, -4px) scale(1.05); opacity: 1; }
    100% { transform: translate(0, 0) scale(1); opacity: 1; }
}

@keyframes send-fly {
    0% { transform: translate(0, 0) scale(1); opacity: 1; }
    30% { transform: translate(6px, -8px) scale(1.15); opacity: 1; }
    60% { transform: translate(10px, -4px) scale(1.05); opacity: 1; }
    100% { transform: translate(0, 0) scale(1); opacity: 1; }
}

@keyframes send-fly {
    0% { transform: translate(0, 0) scale(1); opacity: 1; }
    30% { transform: translate(6px, -8px) scale(1.15); opacity: 1; }
    60% { transform: translate(10px, -4px) scale(1.05); opacity: 1; }
    100% { transform: translate(0, 0) scale(1); opacity: 1; }
}
</style>