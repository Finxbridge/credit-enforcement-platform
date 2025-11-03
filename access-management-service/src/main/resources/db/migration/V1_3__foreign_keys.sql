-- Foreign Keys within the same service boundary

-- User Service: users -> user_groups
ALTER TABLE users
    ADD CONSTRAINT fk_users_user_group FOREIGN KEY (user_group_id)
    REFERENCES user_groups(id) ON DELETE SET NULL;

-- Notice Service: notice_alerts -> notices
ALTER TABLE notice_alerts
    ADD CONSTRAINT fk_alert_notice FOREIGN KEY (notice_id)
    REFERENCES notices(id) ON DELETE CASCADE;