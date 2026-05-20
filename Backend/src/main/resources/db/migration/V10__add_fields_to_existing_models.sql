-- Add subservice_id to orders
ALTER TABLE orders ADD COLUMN subservice_id INT DEFAULT NULL;
ALTER TABLE orders ADD COLUMN secondary_status VARCHAR(50) DEFAULT NULL;
ALTER TABLE orders ADD COLUMN application_code VARCHAR(20) DEFAULT NULL;
ALTER TABLE orders ADD COLUMN service_address VARCHAR(500) DEFAULT NULL;
ALTER TABLE orders ADD COLUMN responsible_department VARCHAR(255) DEFAULT NULL;
ALTER TABLE orders ADD COLUMN signer_user_id INT DEFAULT NULL;
ALTER TABLE orders ADD COLUMN client_edit_enabled TINYINT(1) DEFAULT 0;
ALTER TABLE orders ADD COLUMN form_locked TINYINT(1) DEFAULT 0;

-- Add IIN to users
ALTER TABLE users ADD COLUMN iin VARCHAR(12) DEFAULT NULL;

-- Add foreign key for subservice_id
ALTER TABLE orders ADD CONSTRAINT orders_subservice_fk
  FOREIGN KEY (subservice_id) REFERENCES subservices(id) ON DELETE SET NULL;

-- Add foreign key for signer_user_id
ALTER TABLE orders ADD CONSTRAINT orders_signer_fk
  FOREIGN KEY (signer_user_id) REFERENCES users(id) ON DELETE SET NULL;