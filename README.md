# 🏠 家庭记账本

> 家庭日常财务收支管理系统 — 计算机专业大三期末大作业

## ✨ 功能特性

- 👤 **多用户系统**：注册/登录，JWT Token 认证，每个用户独立账号
- 🏠 **家庭管理**：创建家庭/邀请码加入，管理员邀请/踢出用户，多成员协作记账
- 📝 **收支记账**：快速录入收入/支出，选择类别、日期、成员、备注
- 🤖 **AI 记账助手**：自然语言描述消费，AI 自动解析并创建记录（通义千问）
- 📋 **明细查询**：多条件筛选（类型/类别/成员/日期/关键词），分页列表，权限控制
- 📊 **统计分析**：收支汇总 + 饼图（类别分布）+ 柱状图（成员对比）+ 折线图（月度趋势），支持个人/全部切换
- ⚙️ **系统设置**：类别管理、成员管理、头像设置、数据导出/导入/清空
- 📱 **响应式布局**：适配桌面端和移动端

## 🛠 技术栈

| 层面 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 + Maven |
| 数据库 | MySQL 8.0 |
| ORM | MyBatis-Plus 3.5 |
| 认证 | JWT (jjwt) |
| API 文档 | Knife4j (Swagger) |
| AI | DashScope SDK (通义千问) |
| 前端框架 | Vue 3 + Vite |
| UI 组件库 | Element Plus |
| 图表 | ECharts 5 |
| 工具 | Lombok, Spring Validation |

## 🚀 快速启动

### 环境准备

- JDK 17+
- MySQL 8.0+
- Node.js 18+
- Maven 3.9+

### 1. 创建数据库

```sql
CREATE DATABASE family_account DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改 MySQL 用户名和密码。

### 3. 启动后端

```bash
cd 家庭记账本
mvn spring-boot:run
```

后端启动后访问：
- 主页：http://localhost:8080
- API 文档：http://localhost:8080/doc.html

### 4. 启动前端（开发模式）

```bash
cd 家庭记账本/frontend
npm install
npm run dev
```

前端访问：http://localhost:5173

### 5. 部署到服务器

```bash
# 构建前端
cd frontend && npm install && npm run build

# 复制到后端静态目录
# Windows: xcopy /E /Y dist\* ..\src\main\resources\static\
# Linux:   cp -r dist/* ../src/main/resources/static/

# 打包
cd .. && mvn clean package -DskipTests

# 上传并运行
java -jar target/family-account-1.0.0.jar
```

详细部署说明见 [docs/07-部署说明.md](docs/07-部署说明.md)

## 📁 项目结构

```
家庭记账本/
├── pom.xml                    Maven 配置
├── README.md                  本文件
├── docs/                      项目文档（8份）
├── dev-log/                   开发日志
├── db/                        数据库脚本（init.sql + 迁移脚本）
├── src/main/java/             Spring Boot 后端
│   └── com/familyaccount/
│       ├── controller/        REST 控制器（9个）
│       ├── service/           业务逻辑接口
│       ├── mapper/            MyBatis Mapper
│       ├── entity/            实体类（5个）
│       ├── dto/               数据传输对象（VO + 请求DTO）
│       ├── common/            公共类（Result, JWT, 异常处理, 上下文）
│       └── config/            配置类（CORS, 拦截器, AI配置, 初始化等）
├── src/main/resources/
│   ├── application.yml        应用配置
│   └── mapper/                MyBatis XML
└── frontend/                  Vue 3 前端
    └── src/
        ├── views/             页面组件（8个）
        ├── components/        公共组件（AppLayout, NavBar, SummaryCards）
        ├── api/               API 封装（7个模块）
        ├── router/            路由配置（含路由守卫）
        └── styles/            全局样式
```

## 📖 项目文档

- [01-需求规格说明书](docs/01-需求规格说明书.md)
- [02-技术选型规范](docs/02-技术选型规范.md)
- [03-UI设计规范](docs/03-UI设计规范.md)
- [04-数据库设计文档](docs/04-数据库设计文档.md)
- [05-API接口设计文档](docs/05-API接口设计文档.md)
- [06-开发执行计划](docs/06-开发执行计划.md)
- [07-部署说明](docs/07-部署说明.md)
- [08-云服务器部署清单](docs/08-云服务器部署清单.md)
