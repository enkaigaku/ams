-- Insert sample departments without managers first
INSERT INTO departments (id, name, manager_id) VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', '開発部', NULL),
    ('550e8400-e29b-41d4-a716-446655440002', '営業部', NULL),
    ('550e8400-e29b-41d4-a716-446655440003', '人事部', NULL);

-- Insert sample users (password is 'password123' hashed with BCrypt)
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy (known working hash)
INSERT INTO users (id, employee_id, password_hash, name, email, role, department_id) VALUES 
    -- Managers
    ('550e8400-e29b-41d4-a716-446655440011', 'MGR001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '田中 太郎', 'tanaka@example.com', 'MANAGER', '550e8400-e29b-41d4-a716-446655440001'),
    ('550e8400-e29b-41d4-a716-446655440012', 'MGR002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '佐藤 花子', 'sato@example.com', 'MANAGER', '550e8400-e29b-41d4-a716-446655440002'),
    ('550e8400-e29b-41d4-a716-446655440013', 'MGR003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '鈴木 次郎', 'suzuki@example.com', 'MANAGER', '550e8400-e29b-41d4-a716-446655440003'),
    
    -- Employees
    ('550e8400-e29b-41d4-a716-446655440021', 'EMP001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '山田 一郎', 'yamada@example.com', 'EMPLOYEE', '550e8400-e29b-41d4-a716-446655440001'),
    ('550e8400-e29b-41d4-a716-446655440022', 'EMP002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '高橋 美咲', 'takahashi@example.com', 'EMPLOYEE', '550e8400-e29b-41d4-a716-446655440001'),
    ('550e8400-e29b-41d4-a716-446655440023', 'EMP003', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '渡辺 健太', 'watanabe@example.com', 'EMPLOYEE', '550e8400-e29b-41d4-a716-446655440002'),
    ('550e8400-e29b-41d4-a716-446655440024', 'EMP004', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '伊藤 亜希', 'ito@example.com', 'EMPLOYEE', '550e8400-e29b-41d4-a716-446655440002'),
    ('550e8400-e29b-41d4-a716-446655440025', 'EMP005', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '中村 拓也', 'nakamura@example.com', 'EMPLOYEE', '550e8400-e29b-41d4-a716-446655440003');

-- Update departments with their managers now that users exist
UPDATE departments SET manager_id = 'MGR001' WHERE id = '550e8400-e29b-41d4-a716-446655440001';
UPDATE departments SET manager_id = 'MGR002' WHERE id = '550e8400-e29b-41d4-a716-446655440002';
UPDATE departments SET manager_id = 'MGR003' WHERE id = '550e8400-e29b-41d4-a716-446655440003';

-- Insert sample time records for today and yesterday
INSERT INTO time_records (id, user_id, record_date, clock_in, clock_out, total_hours, status) VALUES 
    -- Today's records
    ('550e8400-e29b-41d4-a716-446655440031', '550e8400-e29b-41d4-a716-446655440021', CURRENT_DATE, CURRENT_DATE + TIME '09:00:00', CURRENT_DATE + TIME '18:00:00', 8.00, 'PRESENT'),
    ('550e8400-e29b-41d4-a716-446655440032', '550e8400-e29b-41d4-a716-446655440022', CURRENT_DATE, CURRENT_DATE + TIME '09:15:00', CURRENT_DATE + TIME '18:15:00', 8.00, 'LATE'),
    ('550e8400-e29b-41d4-a716-446655440033', '550e8400-e29b-41d4-a716-446655440023', CURRENT_DATE, CURRENT_DATE + TIME '09:00:00', NULL, NULL, 'PRESENT'),
    
    -- Yesterday's records
    ('550e8400-e29b-41d4-a716-446655440041', '550e8400-e29b-41d4-a716-446655440021', CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE - INTERVAL '1 day' + TIME '09:00:00', CURRENT_DATE - INTERVAL '1 day' + TIME '17:30:00', 7.50, 'EARLY_LEAVE'),
    ('550e8400-e29b-41d4-a716-446655440042', '550e8400-e29b-41d4-a716-446655440022', CURRENT_DATE - INTERVAL '1 day', CURRENT_DATE - INTERVAL '1 day' + TIME '09:00:00', CURRENT_DATE - INTERVAL '1 day' + TIME '18:00:00', 8.00, 'PRESENT'),
    ('550e8400-e29b-41d4-a716-446655440043', '550e8400-e29b-41d4-a716-446655440024', CURRENT_DATE - INTERVAL '1 day', NULL, NULL, NULL, 'ABSENT');

-- Insert sample leave requests
INSERT INTO leave_requests (id, user_id, type, start_date, end_date, reason, status) VALUES 
    ('550e8400-e29b-41d4-a716-446655440051', '550e8400-e29b-41d4-a716-446655440021', 'ANNUAL', CURRENT_DATE + INTERVAL '7 days', CURRENT_DATE + INTERVAL '7 days', '家族旅行のため', 'PENDING'),
    ('550e8400-e29b-41d4-a716-446655440052', '550e8400-e29b-41d4-a716-446655440022', 'SICK', CURRENT_DATE + INTERVAL '2 days', CURRENT_DATE + INTERVAL '3 days', '風邪による体調不良', 'PENDING'),
    ('550e8400-e29b-41d4-a716-446655440053', '550e8400-e29b-41d4-a716-446655440023', 'ANNUAL', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE - INTERVAL '5 days', '私用', 'APPROVED');

-- Update approved leave request with approver details
UPDATE leave_requests 
SET approved_by = 'MGR002', approved_at = CURRENT_TIMESTAMP - INTERVAL '3 days'
WHERE id = '550e8400-e29b-41d4-a716-446655440053';

-- Insert sample time modification requests
INSERT INTO time_modification_requests (id, user_id, request_date, original_clock_in, original_clock_out, requested_clock_in, requested_clock_out, reason, status) VALUES 
    ('550e8400-e29b-41d4-a716-446655440061', '550e8400-e29b-41d4-a716-446655440021', CURRENT_DATE - INTERVAL '2 days', CURRENT_DATE - INTERVAL '2 days' + TIME '09:15:00', CURRENT_DATE - INTERVAL '2 days' + TIME '18:00:00', CURRENT_DATE - INTERVAL '2 days' + TIME '09:00:00', CURRENT_DATE - INTERVAL '2 days' + TIME '18:00:00', '打刻機の不具合により正しい時刻で打刻できませんでした', 'PENDING'),
    ('550e8400-e29b-41d4-a716-446655440062', '550e8400-e29b-41d4-a716-446655440022', CURRENT_DATE - INTERVAL '1 day', NULL, CURRENT_DATE - INTERVAL '1 day' + TIME '18:00:00', CURRENT_DATE - INTERVAL '1 day' + TIME '09:00:00', CURRENT_DATE - INTERVAL '1 day' + TIME '18:00:00', '朝の打刻を忘れてしまいました', 'PENDING');

-- Insert sample alerts
INSERT INTO alerts (id, type, user_id, alert_date, message, is_read) VALUES 
    ('550e8400-e29b-41d4-a716-446655440071', 'LATE', '550e8400-e29b-41d4-a716-446655440022', CURRENT_DATE, '高橋 美咲さんが2024-08-20に遅刻しました。出勤時刻: 09:15', FALSE),
    ('550e8400-e29b-41d4-a716-446655440072', 'MISSING_CLOCK_OUT', '550e8400-e29b-41d4-a716-446655440023', CURRENT_DATE, '渡辺 健太さんが2024-08-20の退勤打刻を忘れています。', FALSE),
    ('550e8400-e29b-41d4-a716-446655440073', 'ABSENT', '550e8400-e29b-41d4-a716-446655440024', CURRENT_DATE - INTERVAL '1 day', '伊藤 亜希さんが2024-08-19に欠勤しています。', TRUE);