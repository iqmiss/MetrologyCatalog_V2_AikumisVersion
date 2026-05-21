ALTER TABLE subservice_fields
MODIFY COLUMN field_type ENUM('text','number','date','select','file','checkbox') NOT NULL DEFAULT 'text';