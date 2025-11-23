-- STRATEGY ENGINE SERVICE - INDEXES
CREATE INDEX idx_strategies_status ON strategies(status);
CREATE INDEX idx_strategies_last_run_at ON strategies(last_run_at);
CREATE INDEX idx_strategies_priority_status ON strategies(priority DESC, status) WHERE is_active = TRUE;

CREATE INDEX idx_strategy_rules_strategy_id ON strategy_rules(strategy_id);
CREATE INDEX idx_strategy_rules_rule_order ON strategy_rules(rule_order);

CREATE INDEX idx_strategy_actions_strategy_id ON strategy_actions(strategy_id);
CREATE INDEX idx_strategy_actions_action_order ON strategy_actions(action_order);
CREATE INDEX idx_strategy_actions_template_id ON strategy_actions(template_id);

CREATE INDEX idx_strategy_executions_strategy_id ON strategy_executions(strategy_id);
CREATE INDEX idx_strategy_executions_execution_id ON strategy_executions(execution_id);
CREATE INDEX idx_strategy_executions_status ON strategy_executions(execution_status);
CREATE INDEX idx_strategy_executions_started_at ON strategy_executions(started_at DESC);

CREATE INDEX idx_execution_details_execution_id ON strategy_execution_details(execution_id);
CREATE INDEX idx_execution_details_case_id ON strategy_execution_details(case_id);
CREATE INDEX idx_execution_details_action_status ON strategy_execution_details(action_status);

-- Indexes for efficient querying
CREATE INDEX idx_scheduled_jobs_enabled ON scheduled_jobs(is_enabled, next_run_at) WHERE is_enabled = TRUE;
CREATE INDEX idx_scheduled_jobs_service ON scheduled_jobs(service_name, job_type);
CREATE INDEX idx_scheduled_jobs_reference ON scheduled_jobs(job_reference_type, job_reference_id);
CREATE INDEX idx_scheduled_jobs_next_run ON scheduled_jobs(next_run_at) WHERE is_enabled = TRUE;
