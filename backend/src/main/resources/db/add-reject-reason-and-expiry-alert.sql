-- 为 drug_transfer 表添加驳回理由字段
ALTER TABLE drug_transfer ADD COLUMN reject_reason VARCHAR(500) COMMENT '驳回理由' AFTER remark;

-- 创建药品效期预警表
CREATE TABLE IF NOT EXISTS drug_expiry_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inventory_id BIGINT NOT NULL COMMENT '库存记录ID',
    drug_id BIGINT NOT NULL COMMENT '药品ID',
    hospital_id BIGINT NOT NULL COMMENT '机构ID',
    batch_no VARCHAR(50) COMMENT '批号',
    expire_date DATE COMMENT '过期日期',
    days_left INT COMMENT '距离过期天数',
    quantity INT COMMENT '库存数量',
    alert_level VARCHAR(20) NOT NULL DEFAULT 'WARNING' COMMENT '预警级别：WARNING/URGENT',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/RESOLVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_inventory_id (inventory_id),
    INDEX idx_drug_hospital (drug_id, hospital_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
