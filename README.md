# 医共体药品管理系统

## 1. How to Run

### 启动所有服务

```bash
docker-compose up --build -d
```

等待服务启动完成（首次约 3-5 分钟），访问：
- 管理后台：http://localhost:8081
- 后端 API：http://localhost:8080

### 停止并删除数据卷

```bash
docker-compose down -v
```

`-v` 会删除数据卷，清空 MySQL 数据。

### 本地开发

**数据库**：`mysql -u root -p < backend/src/main/resources/db/schema.sql`

**后端**：`cd backend && mvn spring-boot:run`（端口 8080）

**前端**：`cd frontend-admin && npm install && npm run dev`（端口 5173）

## 2. Services

| 服务 | 说明 | 端口 |
|------|------|------|
| frontend-admin | 管理后台 (Vue3 + Element Plus) | 8081 |
| backend | 后端 API (Spring Boot) | 8080 |
| mysql | MySQL 8.0 | 3306 |

**项目结构**：`backend`（后端）、`frontend-admin`（管理后台）。

## 3. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |

## 4. 题目内容

当前文件下使用vue创建医共体药品管理系统

## 5. 项目介绍

医共体药品管理系统面向区域医疗共同体，实现医共体内各级医疗机构（总院、分院、卫生院）的药品统一管理。

**核心功能**：药品信息与分类、库存管理与预警、采购订单、药品调拨。

**技术栈**：Vue 3 + Element Plus + Spring Boot + MyBatis-Plus + MySQL + Docker。

