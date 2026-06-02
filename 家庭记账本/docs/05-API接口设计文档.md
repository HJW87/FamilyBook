# 05 — API 接口设计文档

## 1. 接口规范

### 1.1 基础信息
| 项目 | 值 |
|------|-----|
| 基础路径 | `http://localhost:8080` |
| API 前缀 | `/api` |
| 请求格式 | JSON |
| 响应格式 | JSON |
| 字符编码 | UTF-8 |
| 认证方式 | JWT Bearer Token（Header: `Authorization: Bearer <token>`） |
| API 文档 | `http://localhost:8080/doc.html` (Knife4j) |

### 1.2 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 1.3 错误码定义
| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限（非管理员操作） |
| 404 | 资源不存在 |
| 409 | 数据冲突（如删除被引用的类别） |
| 500 | 服务器内部错误 |

### 1.4 分页响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [...],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

### 1.5 JWT Token 载荷
```json
{
  "sub": "username",
  "userId": 1,
  "familyId": 5,
  "role": "ADMIN",
  "memberId": 3,
  "displayId": 100001,
  "iat": 1717200000,
  "exp": 1717804800
}
```

---

## 2. 认证 API

### 2.1 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456"
}
```
响应：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "zhangsan",
    "displayId": 100001
  }
}
```

### 2.2 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "zhangsan",
  "password": "123456"
}
```
响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "zhangsan",
    "displayId": 100001,
    "familyId": 5,
    "role": "ADMIN",
    "memberId": 3
  }
}
```
> `familyId` 和 `role` 可能为 null（未加入家庭时）

### 2.3 验证 Token
```
GET /api/auth/verify
Authorization: Bearer <token>
```
响应：同登录响应（不含 token）

---

## 3. 家庭管理 API

### 3.1 获取家庭信息
```
GET /api/family/info
Authorization: Bearer <token>
```
响应：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "我的家",
    "inviteCode": "ABC12DEF",
    "adminId": 1
  }
}
```

### 3.2 创建家庭
```
POST /api/family/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "我的家"
}
```
> 创建后自动生成 8 位邀请码、复制 16 个预设类别、创建"管理员"身份标签

### 3.3 加入家庭（通过邀请码）
```
POST /api/family/join
Authorization: Bearer <token>
Content-Type: application/json

{
  "inviteCode": "ABC12DEF",
  "labelName": "儿子"
}
```
> 用户自行填写身份标签名（如"儿子"），标签名在同一家庭内不能重复

### 3.4 获取家庭成员列表
```
GET /api/family/members
Authorization: Bearer <token>
```
响应：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "管理员",
      "familyId": 1,
      "userId": 1,
      "username": "zhangsan",
      "displayId": 100001,
      "createdAt": "2026-06-01T10:00:00"
    },
    {
      "id": 2,
      "name": "儿子",
      "familyId": 1,
      "userId": null,
      "username": null,
      "displayId": null,
      "createdAt": "2026-06-01T10:05:00"
    }
  ]
}
```

### 3.5 添加成员标签（仅管理员）
```
POST /api/family/members
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "爷爷"
}
```
> 403 错误：非管理员调用

### 3.6 删除成员标签（仅管理员，空标签才可删）
```
DELETE /api/family/members/{id}
Authorization: Bearer <token>
```
> 已绑定用户的标签不能删除，需先踢出用户

### 3.7 邀请用户绑定标签（仅管理员，通过展示ID）
```
POST /api/family/invite
Authorization: Bearer <token>
Content-Type: application/json

{
  "memberId": 2,
  "displayId": 100002
}
```
> 目标标签必须为空（userId=null），被邀请用户不能已属于其他家庭

### 3.8 踢出用户（仅管理员）
```
DELETE /api/family/members/{id}/kick
Authorization: Bearer <token>
```
> 将标签的 userId 置 null，标签保留。管理员不能被踢出。

### 3.9 修改自己的标签名
```
PUT /api/family/members/{id}/name
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "爸爸"
}
```
> 只能修改自己绑定的标签（userId 匹配），新名称不能与家庭内其他标签重复

---

## 4. 类别管理 API

> 以下所有 API 均需 JWT 认证（Header: `Authorization: Bearer <token>`），数据均按当前用户的 familyId 隔离。

### 4.1 获取类别列表
```
GET /api/categories?type=EXPENSE
```
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | INCOME / EXPENSE，不传返回全部 |

响应 data：`Category[]`

### 4.2 添加类别
```
POST /api/categories
Content-Type: application/json

{
  "name": "旅游",
  "type": "EXPENSE",
  "icon": "✈️",
  "sortOrder": 5
}
```

### 4.3 修改类别
```
PUT /api/categories/{id}
Content-Type: application/json

{
  "name": "旅行",
  "icon": "🏖️",
  "sortOrder": 6
}
```

### 4.4 删除类别
```
DELETE /api/categories/{id}
```
> 如果该类别被 record 引用，返回 409 错误

---

## 5. 记录管理 API

### 5.1 分页查询记录
```
GET /api/records?page=1&size=20&type=EXPENSE&categoryId=1&familyMember=爸爸&startDate=2026-01-01&endDate=2026-06-01&keyword=午餐
```
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认 1 |
| size | Integer | 否 | 每页条数，默认 20 |
| type | String | 否 | INCOME / EXPENSE |
| categoryId | Long | 否 | 类别ID |
| familyMember | String | 否 | 家庭成员 |
| startDate | String | 否 | 起始日期 YYYY-MM-DD |
| endDate | String | 否 | 截止日期 YYYY-MM-DD |
| keyword | String | 否 | 备注关键词 |

响应 data：`PageResult<RecordVO>`（RecordVO 含关联的 categoryName, categoryIcon）

### 5.2 获取单条记录
```
GET /api/records/{id}
```

### 5.3 添加记录
```
POST /api/records
Content-Type: application/json

{
  "type": "EXPENSE",
  "categoryId": 1,
  "amount": 36.50,
  "familyMember": "爸爸",
  "recordDate": "2026-06-01",
  "note": "午餐外卖"
}
```

### 5.4 修改记录
```
PUT /api/records/{id}
Content-Type: application/json

{
  "type": "EXPENSE",
  "categoryId": 2,
  "amount": 50.00,
  "familyMember": "爸爸",
  "recordDate": "2026-06-01",
  "note": "打车去公司"
}
```

### 5.5 删除记录
```
DELETE /api/records/{id}
```

---

## 6. 统计分析 API

### 6.1 收支汇总
```
GET /api/stats/summary?period=month&startDate=2026-01-01&endDate=2026-06-01
```
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| period | String | 否 | year/month/week/day/custom |
| startDate | String | 否 | 自定义起始日期 |
| endDate | String | 否 | 自定义截止日期 |

响应：
```json
{
  "totalIncome": 12000.00,
  "totalExpense": 8500.00,
  "balance": 3500.00,
  "recordCount": 86,
  "periodLabel": "2026年6月"
}
```

### 6.2 按类别统计
```
GET /api/stats/by-category?period=month&type=EXPENSE
```
响应：
```json
[
  { "categoryId": 1, "categoryName": "餐饮", "icon": "🍔", "count": 25, "total": 3500.00, "percentage": 41.2 },
  { "categoryId": 2, "categoryName": "交通", "icon": "🚗", "count": 12, "total": 1800.00, "percentage": 21.2 }
]
```

### 6.3 按成员统计
```
GET /api/stats/by-member?period=month
```
响应：
```json
[
  { "familyMember": "爸爸", "income": 15000.00, "expense": 5200.00, "count": 35 },
  { "familyMember": "妈妈", "income": 8000.00, "expense": 3300.00, "count": 51 }
]
```

### 6.4 月度趋势
```
GET /api/stats/monthly-trend?months=12
```
响应：
```json
[
  { "month": "2025-07", "income": 12000.00, "expense": 8500.00 },
  { "month": "2025-08", "income": 13000.00, "expense": 9200.00 }
]
```

## 7. 数据管理 API

### 7.1 导出数据
```
GET /api/data/export
```
响应：JSON 文件下载
```json
{
  "version": 1,
  "exportedAt": "2026-06-01T12:00:00",
  "categories": [...],
  "records": [...],
  "familyMembers": [...]
}
```

### 7.2 导入数据
```
POST /api/data/import
Content-Type: multipart/form-data

file: 备份文件.json
```
响应：
```json
{
  "importedCategories": 16,
  "importedRecords": 150,
  "importedMembers": 4
}
```
