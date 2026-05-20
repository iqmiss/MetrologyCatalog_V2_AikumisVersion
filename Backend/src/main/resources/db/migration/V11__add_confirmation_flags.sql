-- Add confirmation flags for internal approval round-trip
ALTER TABLE contracts ADD COLUMN metrolog_confirmed TINYINT(1) DEFAULT 0;
ALTER TABLE contracts ADD COLUMN financier_confirmed TINYINT(1) DEFAULT 0;
ALTER TABLE contracts ADD COLUMN yurist_confirmed TINYINT(1) DEFAULT 0;
ALTER TABLE contracts ADD COLUMN confirmations_requested TINYINT(1) DEFAULT 0;

-- Add yurist role to users
ALTER TABLE users MODIFY COLUMN role ENUM(
  'CLIENT','MANAGER','DIRECTOR','GEN_DIRECTOR',
  'APPROVER','FINANCIER','METROLOG','ADMIN','YURIST'
) NOT NULL;