CREATE INDEX IF NOT EXISTS idx_master_data_type ON master_data(data_type);
CREATE INDEX IF NOT EXISTS idx_master_data_code ON master_data(code);
CREATE INDEX IF NOT EXISTS idx_master_data_parent_code ON master_data(parent_code);
CREATE INDEX IF NOT EXISTS idx_master_data_active ON master_data(is_active);
CREATE INDEX IF NOT EXISTS idx_master_data_display_order ON master_data(display_order);
