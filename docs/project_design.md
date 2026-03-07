# 医共体药品管理系统 - 项目设计文档

## 1. 系统架构

```mermaid
flowchart TD
    subgraph Client["客户端"]
        Admin[管理后台 Vue3]
    end

    subgraph Gateway["网关层"]
        Nginx[Nginx 静态+代理]
    end

    subgraph Backend["后端服务"]
        SpringBoot[Spring Boot API]
    end

    subgraph Data["数据层"]
        MySQL[(MySQL 8.0)]
    end

    Admin -->|HTTP| Nginx
    Nginx -->|/api/*| SpringBoot
    Nginx -->|静态资源| Admin
    SpringBoot -->|JDBC| MySQL
```

## 2. ER 图

```mermaid
erDiagram
    sys_user ||--o| hospital : "belongs_to"
    drug_info }o--|| drug_category : "category_id"
    drug_inventory }o--|| drug_info : "drug_id"
    drug_inventory }o--|| hospital : "hospital_id"
    purchase_order }o--|| hospital : "hospital_id"
    purchase_order }o--o| sys_user : "created_by"
    purchase_order_item }o--|| purchase_order : "order_id"
    purchase_order_item }o--|| drug_info : "drug_id"
    drug_transfer }o--|| hospital : "from_hospital_id"
    drug_transfer }o--|| hospital : "to_hospital_id"
    drug_transfer }o--o| sys_user : "created_by"
    drug_transfer_item }o--|| drug_transfer : "transfer_id"
    drug_transfer_item }o--|| drug_info : "drug_id"
    inventory_log }o--|| drug_info : "drug_id"
    inventory_log }o--|| hospital : "hospital_id"

    sys_user {
        bigint id PK
        varchar username
        varchar password
        bigint hospital_id FK
        varchar role
    }

    hospital {
        bigint id PK
        varchar name
        varchar code
        varchar type
    }

    drug_category {
        bigint id PK
        varchar name
        bigint parent_id
    }

    drug_info {
        bigint id PK
        varchar name
        bigint category_id FK
        decimal price
    }

    drug_inventory {
        bigint id PK
        bigint drug_id FK
        bigint hospital_id FK
        int quantity
        int warning_threshold
    }

    purchase_order {
        bigint id PK
        varchar order_no
        bigint hospital_id FK
        varchar status
    }

    drug_transfer {
        bigint id PK
        varchar transfer_no
        bigint from_hospital_id FK
        bigint to_hospital_id FK
        varchar status
    }

    inventory_log {
        bigint id PK
        bigint drug_id FK
        bigint hospital_id FK
        varchar type
        int quantity
    }
```

## 3. 接口清单

### AuthController `/api/auth`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /login | 登录 |
| GET | /info | 当前用户信息 |
| POST | /logout | 登出 |

### HospitalController `/api/hospitals`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页列表 |
| GET | /list | 全部列表 |
| GET | /{id} | 详情 |
| POST | / | 新增 |
| PUT | /{id} | 更新 |
| DELETE | /{id} | 删除 |

### DrugCategoryController `/api/drug-categories`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /tree | 树形结构 |
| POST | / | 新增 |
| PUT | /{id} | 更新 |
| DELETE | /{id} | 删除 |

### DrugInfoController `/api/drugs`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页列表 |
| GET | /list | 全部列表 |
| GET | /{id} | 详情 |
| POST | / | 新增 |
| PUT | /{id} | 更新 |
| DELETE | /{id} | 删除 |

### InventoryController `/api/inventory`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页列表 |
| GET | /warnings | 预警列表 |
| GET | /quantity | 按药品+机构查库存 |
| GET | /available | 按药品+机构查可用库存 |
| POST | /adjust | 库存调整 |

### InventoryLogController `/api/inventory-logs`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页列表 |

### PurchaseOrderController `/api/purchase-orders`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页列表 |
| GET | /{id} | 详情 |
| POST | / | 新增 |
| PUT | /{id}/approve | 审批 |
| PUT | /{id}/receive | 收货 |
| DELETE | /{id} | 删除 |

### DrugTransferController `/api/transfers`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 分页列表 |
| GET | /{id} | 详情 |
| POST | / | 新增 |
| PUT | /{id}/approve | 审批 |
| PUT | /{id}/reject | 驳回 |
| PUT | /{id}/ship | 发货 |
| PUT | /{id}/complete | 完成 |

### DashboardController `/api/dashboard`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /stats | 统计概览 |

## 4. UI/UX 规范

| 项目 | 规范 |
|------|------|
| 主色调 | #FF7A45（橙） |
| 主色浅 | #FF9A6C |
| 主色深 | #CC5C30 |
| 成功色 | #52C41A |
| 错误色 | #F5222D |
| 主文字 | #333333 |
| 次要文字 | #8C8C8C |
| 卡片圆角 | 8px |
| 卡片阴影 | 0 1px 4px rgba(0,0,0,0.06) |
| 页面背景 | #F5F7FA |
| 卡片背景 | #FFFFFF |
| 字体 | PingFang SC, Microsoft YaHei, sans-serif |
