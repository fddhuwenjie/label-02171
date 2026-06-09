-- 为 drug_transfer 表添加驳回理由字段
ALTER TABLE drug_transfer ADD COLUMN reject_reason VARCHAR(500) DEFAULT NULL COMMENT '驳回理由' AFTER remark;

-- 新建药品效期预警表
CREATE TABLE IF NOT EXISTS drug_expiry_alert (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    drug_id BIGINT NOT NULL COMMENT '药品ID',
    hospital_id BIGINT NOT NULL COMMENT '机构ID',
    inventory_id BIGINT NOT NULL COMMENT '库存记录ID',
    batch_no VARCHAR(50) COMMENT '批号',
    expire_date DATE NOT NULL COMMENT '过期日期',
    days_until_expiry INT NOT NULL COMMENT '距过期天数',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE-生效/RESOLVED-已处理',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_drug_hospital (drug_id, hospital_id),
    INDEX idx_status (status),
    INDEX idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='药品效期预警记录';
