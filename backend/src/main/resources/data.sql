-- 初始化測試資料（可選）
-- 如果不需要可以刪除此檔案

-- 插入範例礦泉水資料
INSERT INTO beverages (name, quantity, production_date, expiry_date, created_at, updated_at)
VALUES 
    ('礦泉水', 100, '2024-01-01', '2025-01-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('礦泉水', 50, '2024-02-01', '2025-02-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('礦泉水', 200, '2024-03-01', '2025-03-01', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

