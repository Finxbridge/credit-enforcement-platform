-- STRATEGY ENGINE SERVICE - TRIGGERS
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE OR REPLACE FUNCTION update_strategy_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.execution_status = 'COMPLETED' AND (OLD.execution_status IS NULL OR OLD.execution_status != 'COMPLETED') THEN
        UPDATE strategies
        SET
            last_run_at = NEW.completed_at,
            success_count = success_count + COALESCE(NEW.successful_actions, 0),
            failure_count = failure_count + COALESCE(NEW.failed_actions, 0),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.strategy_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_execution_strategy_name()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.strategy_name IS NULL THEN
        SELECT strategy_name INTO NEW.strategy_name
        FROM strategies
        WHERE id = NEW.strategy_id;
    END IF;
    IF NEW.execution_id IS NULL THEN
        NEW.execution_id := 'exec_' || NEW.id || '_' || EXTRACT(EPOCH FROM NEW.started_at)::bigint;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql';

CREATE TRIGGER update_strategies_updated_at
    BEFORE UPDATE ON strategies
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_strategy_rules_updated_at
    BEFORE UPDATE ON strategy_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_strategy_actions_updated_at
    BEFORE UPDATE ON strategy_actions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_update_strategy_stats
    AFTER UPDATE ON strategy_executions
    FOR EACH ROW
    WHEN (NEW.execution_status = 'COMPLETED')
    EXECUTE FUNCTION update_strategy_stats();

CREATE TRIGGER trigger_set_execution_strategy_name
    BEFORE INSERT ON strategy_executions
    FOR EACH ROW
    EXECUTE FUNCTION set_execution_strategy_name();
