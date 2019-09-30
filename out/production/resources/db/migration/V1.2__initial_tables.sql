create TABLE user_document
(
    id                                              UUID PRIMARY KEY                DEFAULT uuid_generate_v4(),
    user_id                                         UUID                            NOT NULL,
    document_type                                   VARCHAR(100)                    NOT NULL,
    state                                           VARCHAR(100)                    NOT NULL,
    updated_at                                      TIMESTAMP WITHOUT TIME ZONE     NOT NULL,
    created_at                                      TIMESTAMP WITHOUT TIME ZONE     NOT NULL,
    unique(document_type, user_id)
);

create INDEX user_document_state_idx ON user_document (state);
create INDEX user_document_user_id_idx ON user_document (user_id);
