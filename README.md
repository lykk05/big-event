# big-event

一个基于 Spring Boot 3 + Vue 3 的文章发布与 AI 写作辅助系统。

后端提供用户、文章分类、文章、文件上传与 AI 写作接口；前端提供文章编辑、分类管理、个人信息维护，以及一个聊天式 AI 助手面板（生成标题、推荐分类、润色、续写、扩写、校对）。

## 技术栈

- **后端**：Spring Boot 3.4、MyBatis、PageHelper、MySQL 8、Redis、JWT（java-jwt）、BCrypt、阿里云 OSS
- **前端**：Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios、DOMPurify、Vue Quill
- **AI**：DeepSeek Chat Completions API

## 主要功能

- 用户注册 / 登录 / 登出，JWT + Redis 白名单，支持修改密码后让所有会话失效
- 文章增删改查、条件分页；所有文章与分类操作都带归属校验，防止越权
- 用户资料与头像更新
- 封面图片上传（仅允许图片类型）
- AI 写作助手：生成标题、推荐分类、润色、续写、扩写、校对、自定义指令
- AI 调用按用户每日限流，记录真实 token 用量与耗时

## 环境要求

| 组件 | 版本 |
| --- | --- |
| JDK | **17**（Spring Boot 3.4 与当前 Lombok 版本不支持 JDK 25，Maven 构建请用 JDK 17） |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 18+（推荐 20） |

## 快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < doc/schema.sql
```

脚本会创建 `big_event` 数据库与 `user`、`category`、`article` 三张表。`user.password` 为 `varchar(100)`，用于存放 BCrypt 散列。

### 2. 配置环境变量

后端启动前必须提供：

| 变量 | 说明 |
| --- | --- |
| `DB_PWD` | MySQL 密码 |
| `JWT_KEY` | JWT 签名密钥，至少 32 个字符；缺失时应用启动失败 |
| `DEEPSEEK_API_KEY` | DeepSeek API Key |
| `REDIS_PWD` | 可选，Redis 密码；无密码时留空 |
| `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` | 使用头像/封面上传时必填 |

Windows PowerShell：

```powershell
$env:DB_PWD="your-mysql-password"
$env:JWT_KEY="please-use-a-random-string-at-least-32-chars"
$env:DEEPSEEK_API_KEY="sk-..."
```

Git Bash / Linux / macOS：

```bash
export DB_PWD="your-mysql-password"
export JWT_KEY="please-use-a-random-string-at-least-32-chars"
export DEEPSEEK_API_KEY="sk-..."
```

### 3. 启动 Redis 与后端

```bash
redis-server          # 或启动本机 Redis 服务
mvn spring-boot:run   # 默认 http://localhost:8080
```

如果本机同时装了多个 JDK，先确认 Maven 使用的是 17：

```bash
java -version
mvn -v
```

### 4. 启动前端

```bash
npm install
npm run dev           # 默认 http://localhost:5173，/api 代理到 8080
```

## 接口一览

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/user/register` | 注册（无需登录） |
| POST | `/user/login` | 登录，返回 JWT（无需登录） |
| POST | `/user/logout` | 服务端登出，删除 Redis 中的令牌 |
| GET | `/user/userInfo` | 当前用户信息 |
| PUT | `/user/update` | 更新昵称/邮箱 |
| PATCH | `/user/updateAvatar` | 更新头像 |
| PATCH | `/user/updatePwd` | 修改密码，使该用户所有会话失效 |
| GET / POST / PUT / DELETE | `/article` | 文章分页、新增、更新、删除 |
| GET | `/article/detail?id=` | 文章详情 |
| GET / POST / PUT / DELETE | `/category` | 分类列表、新增、更新、删除 |
| GET | `/category/detail?id=` | 分类详情 |
| POST | `/ai/writing/generate` | AI 写作助手 |
| POST | `/upload` | 图片上传（jpg/jpeg/png/gif/webp） |

除注册、登录外，其余接口都需要请求头 `Authorization: <token>`。

## 测试与构建

```bash
# 后端单元测试（JDK 17）
mvn test

# 前端静态检查与构建
npm run lint
npm run build
```

CI 配置见 `.github/workflows/ci.yml`，会运行后端测试、前端 lint 与构建。

## 安全说明

- 口令使用 BCrypt 存储；存量 MD5 用户登录成功后自动升级
- JWT 只携带 `id`、`username`，不携带口令散列
- 文章、分类接口在 SQL 与 Service 两层校验归属，防止 IDOR
- AI 返回的 HTML 在渲染前经 DOMPurify 净化
- AI 接口按用户每日限流，可通过 `ai.rate-limit.daily` 调整

## 进一步改进

项目的改进清单、优先级与验收方式见 [doc/项目改进建议.md](doc/项目改进建议.md)。
