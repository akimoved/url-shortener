# URL Shortener Service

Сервис для сокращения ссылок с использованием Spring Boot, PostgreSQL и Docker.

## Быстрый старт

### Запустить через Docker Compose

```bash
docker-compose up --build
```

Сервис будет доступен на `http://localhost:8080`

## API Endpoints

### Сократить URL

**POST** `/api/shorten`

Request:
```json
{
  "url": "https://example.com/very/long/url"
}
```

Response (201 Created):
```json
{
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://example.com/very/long/url"
}
```

### Редирект на оригинальный URL

**GET** `/{shortCode}`

Response: 302 Redirect на оригинальный URL

## Пример использования

### cURL

```bash
# Сократить URL
curl -X POST http://localhost:8080/api/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'

# Получить редирект
curl -L http://localhost:8080/abc123
```

## Конфигурация

Основные параметры в `application.yml`:

```yaml
app:
  short-url:
    base-url: http://localhost:8080  # Базовый URL для коротких ссылок
    code-length: 6                   # Длина короткого кода
```

## База данных

Схема создается автоматически через Liquibase миграции при старте приложения.

**Таблица urls:**
- `id` - PRIMARY KEY
- `short_code` - уникальный код (индекс)
- `original_url` - оригинальная ссылка
- `created_at` - время создания

## Особенности реализации

- Алгоритм генерации короткого кода: SHA-256 хеш от URL + nanoTime, затем Base64
- Дедупликация: если URL уже существует, возвращается существующий короткий код
- Транзакционность: использование Spring @Transactional
- Валидация входных данных через Bean Validation
- Обработка ошибок через @RestControllerAdvice