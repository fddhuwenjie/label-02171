-- Migration: 添加调拨单驳回理由字段 + 药品效期预警表
-- 日期: 2026-06-09

USE med_community;

-- 1. 为 drug_transfer 表添加 reject_reason 字段
ALTER TABLE drug_transfer
    ADD COLUMN reject_reason VARCHAR(500) NULL COMMENT '驳回理由' AFTER remark;

-- 2. 创建 drug_expiry_alert 药品效期预警表
CREATE TABLE drug_expiry_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL COMMENT '关联库存ID',
    drug_id BIGINT NOT NULL COMMENT '药品ID',
    hospital_id BIGINT NOT NULL COMMENT '机构ID',
    batch_no VARCHAR(50) COMMENT '批号',
    expire_date DATE NOT NULL COMMENT '过期日期',
    days_to_expire INT NOT NULL COMMENT '距离过期天数（负数表示已过期）',
    quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    alert_level VARCHAR(20) NOT NULL COMMENT 'RED(30天内)/YELLOW(31-90天)/EXPIRED(已过期)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/RESOLVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_inventory_id (inventory_id),
    INDEX idx_hospital_id (hospital_id),
    INDEX idx_status (status),
    INDEX idx_alert_level (alert_level),
    INDEX idx_days (days_to_expire)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品效期预警表';
