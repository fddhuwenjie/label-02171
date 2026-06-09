CREATE DATABASE IF NOT EXISTS med_community DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE med_community;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    hospital_id BIGINT,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'ADMIN/USER',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-disabled 1-enabled',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE hospital (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL COMMENT 'CENTER/BRANCH/CLINIC',
    address VARCHAR(200),
    contact VARCHAR(50),
    phone VARCHAR(20),
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drug_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT 0,
    sort_order INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drug_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    generic_name VARCHAR(100),
    code VARCHAR(50) NOT NULL UNIQUE,
    category_id BIGINT,
    manufacturer VARCHAR(100),
    spec VARCHAR(100) COMMENT 'specification',
    unit VARCHAR(20) COMMENT 'unit',
    dosage_form VARCHAR(50) COMMENT 'dosage form',
    approval_number VARCHAR(50),
    price DECIMAL(10,2) DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_id (category_id),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drug_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drug_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    warning_threshold INT DEFAULT 100,
    batch_no VARCHAR(50),
    expire_date DATE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_drug_hospital (drug_id, hospital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE,
    hospital_id BIGINT NOT NULL,
    supplier VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/DELIVERED/COMPLETED',
    total_amount DECIMAL(12,2) DEFAULT 0,
    remark VARCHAR(500),
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_hospital_id (hospital_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE purchase_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    amount DECIMAL(12,2),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drug_transfer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transfer_no VARCHAR(50) NOT NULL UNIQUE,
    from_hospital_id BIGINT NOT NULL,
    to_hospital_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/SHIPPING/COMPLETED/REJECTED',
    remark VARCHAR(500),
    reject_reason VARCHAR(500) COMMENT '驳回理由',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_from_hospital (from_hospital_id),
    INDEX idx_to_hospital (to_hospital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drug_transfer_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    transfer_id BIGINT NOT NULL,
    drug_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    batch_no VARCHAR(50),
    INDEX idx_transfer_id (transfer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inventory_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drug_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'IN/OUT/TRANSFER_IN/TRANSFER_OUT/ADJUST',
    quantity INT NOT NULL,
    batch_no VARCHAR(50),
    operator VARCHAR(50),
    remark VARCHAR(200),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_drug_hospital (drug_id, hospital_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE drug_expiry_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL COMMENT '关联库存记录ID',
    drug_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    batch_no VARCHAR(50),
    expire_date DATE NOT NULL COMMENT '过期日期',
    days_to_expire INT NOT NULL COMMENT '距离过期剩余天数',
    alert_level VARCHAR(20) NOT NULL DEFAULT 'WARNING' COMMENT 'WARNING/CRITICAL/EXPIRED',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-已处理 1-未处理',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inventory (inventory_id),
    INDEX idx_drug_hospital (drug_id, hospital_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
