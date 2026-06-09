-- ============================================================
-- Migration: 调拨驳回理由 & 药品效期预警
-- 1) drug_transfer 增加 reject_reason 字段
-- 2) 新建 drug_expiry_alert 表，存储效期预警记录
-- ============================================================

-- 1) 调拨表增加驳回理由字段
ALTER TABLE drug_transfer
    ADD COLUMN reject_reason VARCHAR(500) NULL COMMENT '驳回理由' AFTER remark;

-- 2) 效期预警表
CREATE TABLE IF NOT EXISTS drug_expiry_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL COMMENT '关联的库存记录ID',
    drug_id BIGINT NOT NULL,
    hospital_id BIGINT NOT NULL,
    batch_no VARCHAR(50) COMMENT '批号',
    expire_date DATE NOT NULL COMMENT '过期日期',
    days_to_expire INT NOT NULL COMMENT '距离过期剩余天数（生成时快照）',
    alert_level VARCHAR(20) NOT NULL DEFAULT 'WARNING' COMMENT 'WARNING/CRITICAL/EXPIRED',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0-已处理 1-未处理',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inventory (inventory_id),
    INDEX idx_drug_hospital (drug_id, hospital_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
