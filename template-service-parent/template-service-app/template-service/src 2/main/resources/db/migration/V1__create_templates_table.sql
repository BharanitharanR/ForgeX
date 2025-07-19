CREATE TABLE templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    input_schema TEXT,
    output_schema TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE template_metadata (
    template_id BIGINT NOT NULL,
    key VARCHAR(255) NOT NULL,
    value TEXT,
    PRIMARY KEY (template_id, key),
    FOREIGN KEY (template_id) REFERENCES templates(id) ON DELETE CASCADE
);

CREATE INDEX idx_templates_name ON templates(name);
CREATE INDEX idx_templates_created_at ON templates(created_at); 