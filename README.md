# MetrologyCatalog

Веб-платформа для управления метрологическими услугами. Позволяет клиентам подавать заявки на поверку приборов, метрологам обрабатывать их в очереди, менеджерам просматривать статистику, а администраторам управлять пользователями.

## Технологии

| Уровень | Стек |
|---------|------|
| Backend | Java 21, Spring Boot 3.5, Spring Security, JWT, Spring Data JPA, HikariCP, iText 7 |
| Frontend | React 19, TypeScript, Vite, Zustand, Axios |
| База данных | MySQL 8.0 |
| Email | Spring Mail + Mailtrap SMTP |

## Структура проекта
```
ServiceCatalog/
├── Backend/    # Spring Boot приложение
├── Frontend/   # React приложение
└── database.sql # Схема БД и тестовые данные
```

## Быстрый старт

### 1. База данных

Создай БД и выполни схему:
```sql
mysql -u root -p < database.sql
```

### 2. Backend

Создай файл `Backend/src/main/resources/application.properties`:
```properties
spring.application.name=metrology-catalog
server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/service_catalog
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

spring.mail.host=sandbox.smtp.mailtrap.io
spring.mail.port=2525
spring.mail.username=YOUR_MAILTRAP_USERNAME
spring.mail.password=YOUR_MAILTRAP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

jwt.secret=metrology-catalog-secret-key-2026-very-long-string
jwt.expiration=86400000
```

Запусти бэкенд:
```bash
cd Backend
mvn spring-boot:run
```

### 3. Frontend
```bash
cd Frontend
npm install
npm run dev
```

Фронтенд доступен на `http://localhost:5173`

## Тестовые аккаунты

| Роль | Email | Пароль |
|------|-------|--------|
| Клиент | client@test.kz | password |
| Метролог | metrolog@test.kz | password |
| Менеджер | manager@metrology.kz | password |
| Администратор | admin@metrology.kz | password |

## Роли пользователей

- **Клиент** — подача заявок, оплата, скачивание договоров и сертификатов
- **Метролог** — обработка очереди заявок, завершение с созданием результата
- **Менеджер** — просмотр дашборда со статистикой и отчётов
- **Администратор** — управление пользователями (роли, блокировка)

## API

Backend запускается на `http://localhost:8081/api`

Основные endpoints:
- `POST /auth/login` — вход
- `POST /auth/register` — регистрация
- `GET /services` — каталог услуг
- `GET /orders` — список заявок
- `PUT /orders/{id}/status` — смена статуса
- `GET /pdf/certificate/{orderId}` — скачать сертификат
- `GET /contracts/{orderId}/download` — скачать договор