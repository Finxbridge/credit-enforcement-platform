-- Strategy Engine Service - Triggers
-- Version 5.2 - Auto-update triggers

-- Function to auto-update strategy statistics
CREATE OR REPLACE FUNCTION update_strategy_stats()
RETURNS TRIGGER AS $$
BEGIN
    -- Update strategy statistics when execution completes
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

-- Trigger for strategy statistics
DROP TRIGGER IF EXISTS trigger_update_strategy_stats ON strategy_executions;
CREATE TRIGGER trigger_update_strategy_stats
    AFTER UPDATE ON strategy_executions
    FOR EACH ROW
    WHEN (NEW.execution_status = 'COMPLETED')
    EXECUTE FUNCTION update_strategy_stats();

-- Function to set strategy_name in executions
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
$$ LANGUAGE plpgsql;

-- Trigger for execution strategy name
DROP TRIGGER IF EXISTS trigger_set_execution_strategy_name ON strategy_executions;
CREATE TRIGGER trigger_set_execution_strategy_name
    BEFORE INSERT ON strategy_executions
    FOR EACH ROW
    EXECUTE FUNCTION set_execution_strategy_name();

COMMENT ON FUNCTION update_strategy_stats() IS 'Auto-updates strategy statistics when execution completes';
COMMENT ON FUNCTION set_execution_strategy_name() IS 'Auto-populates strategy_name and execution_id';
