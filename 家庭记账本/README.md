# 🏠 家庭记账本

> 家庭日常财务收支管理系统 — 计算机专业大三期末大作业

## ✨ 功能特性

- 📝 **收支记账**：快速录入收入/支出，选择类别、日期、成员、备注
- 📋 **明细查询**：多条件筛选（类型/类别/成员/日期/关键词），分页列表
- 📊 **统计分析**：收支汇总 + 饼图（类别分布）+ 柱状图（成员对比）+ 折线图（月度趋势）
- ⚙️ **系统设置**：类别管理、成员管理、数据导出/导入/清空

## 🛠 技术栈

| 层面 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 + Maven |
| 数据库 | MySQL 8.0 |
| ORM | MyBatis-Plus 3.5 |
| API 文档 | Knife4j (Swagger) |
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
├── docs/                      项目文档（7份）
├── dev-log/                   开发日志
├── db/init.sql                数据库初始化脚本
├── src/main/java/             Spring Boot 后端
│   └── com/familyaccount/
│       ├── controller/        REST 控制器
│       ├── service/           业务逻辑
│       ├── mapper/            MyBatis Mapper
│       ├── entity/            实体类
│       ├── dto/               数据传输对象
│       ├── common/            公共类（Result, 异常处理）
│       └── config/            配置类
├── src/main/resources/
│   ├── application.yml        应用配置
│   └── mapper/                MyBatis XML
└── frontend/                  Vue 3 前端
    └── src/
        ├── views/             页面组件（4个）
        ├── components/        公共组件
        ├── api/               API 封装
        ├── router/            路由配置
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
