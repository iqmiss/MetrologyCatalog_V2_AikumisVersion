ALTER TABLE services ADD COLUMN code VARCHAR(10) DEFAULT NULL;

-- Update service codes
UPDATE services SET code = 'ИТ'  WHERE id = 1;
UPDATE services SET code = 'МА'  WHERE id = 2;
UPDATE services SET code = 'МВИ' WHERE id = 3;
UPDATE services SET code = 'АИО' WHERE id = 4;
UPDATE services SET code = 'ДСО' WHERE id = 5;
UPDATE services SET code = 'ПСИ' WHERE id = 6;
UPDATE services SET code = 'КСИ' WHERE id = 7;
UPDATE services SET code = 'ИПК' WHERE id = 8;
UPDATE services SET code = 'МЛС' WHERE id = 9;
UPDATE services SET code = 'АП'  WHERE id = 10;
UPDATE services SET code = 'ПРИ' WHERE id = 11;
UPDATE services SET code = 'ПРЗ' WHERE id = 12;

-- Insert subservices
INSERT INTO subservices (service_id, name, code, description) VALUES
(1, 'Испытания СИ для целей утверждения типа (серийное производство)', 'УТ',  'Заявка на проведение испытаний средств измерений для целей утверждения типа (серийное производство)'),
(1, 'Испытания СИ для целей утверждения типа партии',                  'УТП', 'Заявка на испытания средств измерений для целей утверждения типа партии'),
(1, 'Испытания СИ на соответствие утвержденному типу',                 'СУТ', 'Заявка на испытания средств измерений на соответствие утвержденному типу'),
(2, 'Метрологическая аттестация средств измерений',                    'А',   'Заявка на проведение метрологической аттестации средств измерений'),
(9, 'Анкета участника МЛС',                                            'А',   'Анкета участника межлабораторных сличений');