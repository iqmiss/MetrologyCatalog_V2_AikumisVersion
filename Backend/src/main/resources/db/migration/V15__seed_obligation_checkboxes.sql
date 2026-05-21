ALTER TABLE subservice_fields MODIFY COLUMN label_ru TEXT NOT NULL;


-- ИТ_УТ obligation checkbox
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(1, 'obligation', 'Заявитель обязуется оплатить все расходы по проведению испытаний средств измерений, рассмотрению материалов и осуществлению других услуг, связанных с испытаниями и утверждением типа средств измерений в соответствии с условиями заключенных договоров. Заявитель гарантирует в течение срока действия сертификата об утверждении типа соответствие произведенных средств измерений утвержденному типу.', 'checkbox', 1, NULL, 99, 0);

-- ИТ_УТП obligation checkbox
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(2, 'obligation', 'Заявитель обязуется оплатить все расходы по проведению испытаний средств измерений, рассмотрению материалов и осуществлению других услуг, связанных с испытаниями и утверждением типа средств измерений в соответствии с условиями заключенных договоров. Заявитель гарантирует в течение срока действия сертификата об утверждении типа соответствие произведенных средств измерений утвержденному типу.', 'checkbox', 1, NULL, 99, 0);

-- ИТ_СУТ obligation checkbox
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(3, 'obligation', 'Заявитель обязуется оплатить все расходы по проведению испытаний средств измерений, рассмотрению материалов и осуществлению других услуг, связанных с испытаниями и утверждением типа средств измерений в соответствии с условиями заключенных договоров. Заявитель гарантирует в течение срока действия сертификата об утверждении типа соответствие произведенных средств измерений утвержденному типу.', 'checkbox', 1, NULL, 99, 0);

-- МА_А obligation checkbox
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(4, 'obligation', 'Обязуемся оплатить все расходы по проведению экспериментальных работ, рассмотрению материалов и осуществлению других услуг, связанных с метрологической аттестацией средств измерений в соответствии с условиями заключённых договоров.', 'checkbox', 1, NULL, 99, 0);