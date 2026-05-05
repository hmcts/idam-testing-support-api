CREATE INDEX IF NOT EXISTS idx_testing_entity_testing_session_id_entity_type_state
    ON testing_entity(testing_session_id, entity_type, state);
