-- Create table for Customer.tags element collection expected by JPA
CREATE TABLE IF NOT EXISTS customer_tags (
    customer_id BIGINT REFERENCES customers(id) ON DELETE CASCADE,
    tags VARCHAR(255)
);

-- Optional composite key to avoid duplicates
DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_schema = 'public' AND table_name = 'customer_tags' AND constraint_name = 'pk_customer_tags'
    ) THEN
        ALTER TABLE customer_tags
        ADD CONSTRAINT pk_customer_tags PRIMARY KEY (customer_id, tags);
    END IF;
END
$$;
