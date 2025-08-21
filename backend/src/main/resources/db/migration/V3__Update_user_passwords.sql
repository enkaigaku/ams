-- Update user passwords with correct BCrypt hash for 'password123'
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE employee_id IN ('MGR001', 'MGR002', 'MGR003', 'EMP001', 'EMP002', 'EMP003', 'EMP004', 'EMP005');