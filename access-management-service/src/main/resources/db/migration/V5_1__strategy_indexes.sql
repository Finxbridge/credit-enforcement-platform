-- Strategy Engine Service - Additional Indexes
-- Version 5.1 - Performance indexes for new columns

-- Strategies table indexes
CREATE INDEX IF NOT EXISTS idx_strategies_status ON strategies(status);
CREATE INDEX IF NOT EXISTS idx_strategies_last_run_at ON strategies(last_run_at);
CREATE INDEX IF NOT EXISTS idx_strategies_schedule_expression ON strategies(schedule_expression) WHERE schedule_expression IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_strategies_event_type ON strategies(event_type) WHERE event_type IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_strategies_priority_status ON strategies(priority DESC, status) WHERE is_active = TRUE;

-- Strategy actions indexes
CREATE INDEX IF NOT EXISTS idx_strategy_actions_template_id ON strategy_actions(template_id) WHERE template_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_strategy_actions_channel ON strategy_actions(channel) WHERE channel IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_strategy_actions_priority ON strategy_actions(priority);

-- Strategy executions indexes
CREATE INDEX IF NOT EXISTS idx_strategy_executions_execution_id ON strategy_executions(execution_id);
CREATE INDEX IF NOT EXISTS idx_strategy_executions_strategy_name ON strategy_executions(strategy_name);
CREATE INDEX IF NOT EXISTS idx_strategy_executions_status ON strategy_executions(execution_status);
CREATE INDEX IF NOT EXISTS idx_strategy_executions_started_at ON strategy_executions(started_at DESC);
CREATE INDEX IF NOT EXISTS idx_strategy_executions_completed_at ON strategy_executions(completed_at DESC) WHERE completed_at IS NOT NULL;

-- Strategy rules indexes
CREATE INDEX IF NOT EXISTS idx_strategy_rules_field_name ON strategy_rules(field_name) WHERE field_name IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_strategy_rules_operator ON strategy_rules(operator) WHERE operator IS NOT NULL;

-- Composite indexes for common queries
CREATE INDEX IF NOT EXISTS idx_strategies_active_lookup
    ON strategies(is_active, status, priority DESC)
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_executions_strategy_lookup
    ON strategy_executions(strategy_id, started_at DESC);
