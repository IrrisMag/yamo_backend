-- Create table for Receipt entity expected by JPA
CREATE TABLE IF NOT EXISTS receipts (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT UNIQUE REFERENCES orders(id) ON DELETE CASCADE,
    issue_date TIMESTAMP,
    pdf_path VARCHAR(1024)
);
