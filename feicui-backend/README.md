# 高翠网 - AI 翡翠匹配平台后端

移动端 H5 的翡翠找货/卖货平台。买家通过 AI 对话找货，商家入驻发布货源、获取客资。

## 技术栈

| 类别 | 选型 |
|---|---|
| 框架 | Spring Boot 3.2.5 + JDK 21 |
| 持久层 | MyBatis-Plus 3.5.7 + MySQL 8 |
| AI | LangChain4j 1.0.1 + 阿里 DashScope（Qwen-Plus 文本 / Qwen-VL-Max 视觉） |
| 存储 | 阿里云 OSS（商品图片） |
| 鉴权 | JWT（com.auth0:java-jwt） |
| 文档 | Knife4j (OpenAPI3) |
| 工具 | Hutool |

> ⚠️ 本环境 Maven 无法生成 Lombok 代码，**全程手写 getter/setter**，不使用 Lombok。

## 目录结构

```
feicui-backend/
├── src/main/java/com/gaocui/
│   ├── FeicuiApplication.java        启动类
│   ├── common/                       统一响应/异常/JWT/拦截器/OSS/配置
│   └── modules/
│       ├── auth/         邮箱验证码登录注册
│       ├── merchant/     商家资料/面板/通知设置/分层
│       ├── product/      商品CRUD/上下架/额度/管理列表
│       ├── lead/         客资提交/列表/详情/脱敏
│       ├── ai/           找货匹配 + 图片转文案
│       ├── notify/       系统通知 + VIP到期定时任务
│       └── home/         游客公开接口(商品详情/留资/AI匹配)
└── src/main/resources/
    ├── application.yml / application-dev.yml
    └── db/schema.sql    建库建表脚本
```

## 启动

1. **建库**：执行 `src/main/resources/db/schema.sql`（MySQL 8，会创建 `gaocui` 库与 5 张表）。
2. **改配置**：编辑 `application-dev.yml`：
   - `spring.datasource` 数据库账号密码
   - `oss.*` 阿里云 OSS（endpoint/bucket/AK/SK/domain，需有效 AccessKey）
   - `dashscope.api-key` 阿里 DashScope API Key
3. **运行**：`mvn spring-boot:run`，启动后访问接口文档 `http://localhost:8080/api/doc.html`。

> 验证码开发期直接**控制台打印**（`gaocui.verify-code.dev-print=true`），无需真实邮箱。

## URL 鉴权约定

- `/auth/**`、`/home/**`、`/files/**` ：游客可访问（无需登录）
- `/merchant/**` ：需在 `Authorization: Bearer <token>` 头携带 JWT

## 核心 API

### 鉴权 `/auth`
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/send-code` | 发送邮箱验证码（控制台打印） |
| POST | `/auth/login` | 邮箱验证码登录/注册（二合一），返回 JWT |

### 商家 `/merchant`
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/merchant/profile` | 个人中心资料（含生效层级、发布上限） |
| GET | `/merchant/dashboard` | 数据面板（上架数/上限、今日/累计客资） |
| PUT | `/merchant/email` | 修改邮箱 |
| PUT | `/merchant/notification-settings` | 更新通知开关 |
| POST | `/merchant/logout` | 退出登录 |

### 商品 `/merchant/products`
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/upload-image` | 上传图片到 OSS，返回 URL |
| POST | `/merchant/products/ai-generate` | AI 图片转文案（Qwen-VL） |
| POST | `` | 创建草稿 |
| PUT | `/{id}` | 编辑 |
| POST | `/{id}/publish` | 发布（草稿→上架，校验额度） |
| PUT | `/{id}/status` | 上下架切换 |
| DELETE | `/{id}` | 删除 |
| GET | `?status=&current=&size=` | 管理列表（全部/已上架/草稿/已下架） |
| GET | `/{id}` | 商家自己的详情 |

### 客资 `/merchant/leads`
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `?status=&current=&size=` | 客资列表（FREE 邮箱脱敏） |
| GET | `/{id}` | 客资详情（FREE 邮箱脱敏） |
| PUT | `/{id}/contacted` | 标记已联系 |

### 系统通知 `/merchant/notifications`
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `?current=&size=` | 通知列表 |
| GET | `/unread-count` | 未读数 |
| PUT | `/{id}/read` | 标记已读 |
| PUT | `/read-all` | 全部已读 |

### 游客 `/home`
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/home/ai/match` | AI 找货匹配（解析需求→3 张翡翠卡片） |
| GET | `/home/products/{id}` | 商品详情（仅已上架） |
| POST | `/home/products/{id}/lead` | 提交客资（留邮箱留言） |

## 业务规则要点

- **分层**：FREE 发布上限 2 件、客资邮箱脱敏；VIP 上限 100 件、邮箱可见。VIP 过期自动降级展示。
- **商品状态机**：DRAFT（草稿）→ LISTED（已上架）↔ DELISTED（已下架）。上架前校验额度。
- **客资**：游客在商品详情留邮箱+留言，进入该商品所属商家。FREE 看脱敏邮箱。
- **通知**：新客资自动生成站内通知；每天 09:00 扫描 30 天内到期 VIP 并提醒（去重）。
