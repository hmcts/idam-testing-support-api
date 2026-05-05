CREATE INDEX IF NOT EXISTS idx_testing_entity_entity_id_entity_type_state
    ON testing_entity(entity_id, entity_type, state);
