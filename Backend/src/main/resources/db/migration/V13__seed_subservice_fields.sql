-- ИТ_УТ (subservice_id=1) — Серийное производство
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(1, 'si_name', 'Наименование и обозначение (тип/модели) СИ', 'text', 1, NULL, 1, 1),
(1, 'characteristics', 'Основные метрологические характеристики или номер реестра ГСИ РК', 'text', 1, NULL, 2, 1),
(1, 'producer', 'Наименование производителя, адрес', 'text', 1, NULL, 3, 1),
(1, 'has_software', 'Наличие программного обеспечения СИ', 'select', 1, '["Да","Нет"]', 4, 1),
(1, 'date_place', 'Предполагаемая дата и место проведения испытаний', 'text', 1, NULL, 5, 1),
(1, 'doc_representative', 'Документ о статусе официального представителя завода-изготовителя', 'file', 1, NULL, 6, 0),
(1, 'doc_operational', 'Эксплуатационные документы производителя', 'file', 1, NULL, 7, 0),
(1, 'doc_publication', 'Письмо о допустимости опубликования описания типа', 'file', 1, NULL, 8, 0);

-- ИТ_УТП (subservice_id=2) — Утверждение типа партии
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(2, 'si_name', 'Наименование и обозначение (тип/модели) СИ', 'text', 1, NULL, 1, 1),
(2, 'characteristics', 'Основные метрологические характеристики или номер реестра ГСИ РК', 'text', 1, NULL, 2, 1),
(2, 'producer', 'Наименование производителя, адрес', 'text', 1, NULL, 3, 1),
(2, 'has_software', 'Наличие программного обеспечения СИ', 'select', 1, '["Да","Нет"]', 4, 1),
(2, 'serial_numbers', 'Заводские номера СИ (или диапазон)', 'text', 1, NULL, 5, 1),
(2, 'date_place', 'Предполагаемая дата и место проведения испытаний', 'text', 1, NULL, 6, 1),
(2, 'doc_operational', 'Эксплуатационные документы производителя', 'file', 1, NULL, 7, 0),
(2, 'doc_publication', 'Письмо о допустимости опубликования описания типа', 'file', 1, NULL, 8, 0);

-- ИТ_СУТ (subservice_id=3) — Соответствие утвержденному типу
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(3, 'si_name', 'Наименование и обозначение (тип/модели) СИ', 'text', 1, NULL, 1, 1),
(3, 'characteristics', 'Основные метрологические характеристики или номер реестра ГСИ РК', 'text', 1, NULL, 2, 1),
(3, 'producer', 'Наименование производителя, адрес', 'text', 1, NULL, 3, 1),
(3, 'modified_characteristics', 'Наименование измененных характеристик СИ с модификацией', 'text', 1, NULL, 4, 1),
(3, 'has_software', 'Наличие программного обеспечения СИ', 'select', 1, '["Да","Нет"]', 5, 1),
(3, 'date_place', 'Предполагаемая дата и место проведения испытаний', 'text', 1, NULL, 6, 1),
(3, 'doc_certificate', 'Копия сертификата об утверждении типа', 'file', 1, NULL, 7, 0),
(3, 'doc_act', 'Копия акта испытаний СИ', 'file', 1, NULL, 8, 0),
(3, 'doc_operational', 'Эксплуатационные документы производителя', 'file', 1, NULL, 9, 0),
(3, 'doc_representative', 'Документ о статусе официального представителя', 'file', 1, NULL, 10, 0),
(3, 'doc_software', 'Заявка на аттестацию программного обеспечения', 'file', 0, NULL, 11, 0);

-- МА_А (subservice_id=4) — Метрологическая аттестация
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(4, 'si_name', 'Наименование и обозначение типа СИ', 'text', 1, NULL, 1, 1),
(4, 'producer', 'Наименование производителя, страна', 'text', 1, NULL, 2, 1),
(4, 'characteristics', 'Основные метрологические характеристики', 'text', 1, NULL, 3, 1),
(4, 'has_software', 'Наличие программного обеспечения СИ', 'select', 1, '["Да","Нет"]', 4, 1),
(4, 'quantity', 'Количество, шт.', 'number', 1, NULL, 5, 1),
(4, 'date_place', 'Предполагаемая дата и место проведения', 'text', 1, NULL, 6, 1),
(4, 'doc_operational', 'Эксплуатационные документы производителя', 'file', 1, NULL, 7, 0),
(4, 'doc_program', 'Проект программы метрологической аттестации', 'file', 0, NULL, 8, 0),
(4, 'doc_method', 'Проект методики поверки', 'file', 0, NULL, 9, 0);

-- МЛС_А (subservice_id=5) — Анкета участника МЛС
INSERT INTO subservice_fields (subservice_id, field_key, label_ru, field_type, required, options_json, sort_order, is_repeating) VALUES
(5, 'org_name', 'Полное и сокращенное наименование организации', 'text', 1, NULL, 1, 0),
(5, 'director_name', 'ФИО и должность руководителя (подписанта)', 'text', 1, NULL, 2, 0),
(5, 'requisites', 'Реквизиты предприятия (БИН, ИИК, банк)', 'text', 1, NULL, 3, 0),
(5, 'legal_address', 'Юридический адрес организации', 'text', 1, NULL, 4, 0),
(5, 'mailing_address', 'Адрес для рассылки объектов сличений', 'text', 1, NULL, 5, 0),
(5, 'program_type', 'Наименование программы МЛС', 'select', 1, '["Поверка","Калибровка","Испытания"]', 6, 0),
(5, 'si_description', 'Наименование и тип СИ с метрологическими характеристиками', 'text', 1, NULL, 7, 0),
(5, 'contact_name', 'Контактное лицо ФИО', 'text', 1, NULL, 8, 0),
(5, 'contact_position', 'Должность контактного лица', 'text', 1, NULL, 9, 0),
(5, 'contact_phone', 'Телефон контактного лица', 'text', 1, NULL, 10, 0),
(5, 'contact_email', 'Email контактного лица', 'text', 1, NULL, 11, 0),
(5, 'needs_contract', 'Нужен договор или достаточно счета', 'select', 1, '["Договор","Счет","Оба"]', 12, 0);