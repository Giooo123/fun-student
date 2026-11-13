# Fun Student

Приложение для управления студентами и пользователями. Java 21, Spring Boot 4, PostgreSQL, HTML, CSS, JavaScript.

## Запуск на macOS

Установить Java 21 и PostgreSQL 16 или новее. Для установки PostgreSQL через Homebrew:

```bash
brew install postgresql@16
brew services start postgresql@16
```

При установке через Homebrew часто создаётся пользователь PostgreSQL с именем текущего пользователя macOS. Чтобы использовать его вместо `postgres`, перед запуском выполнить:

```bash
export DB_USER="$(whoami)"
export DB_PASSWORD=""
```

Если PostgreSQL установлен с пользователем `postgres` и паролем `postgres`, переменные задавать не требуется. Если пароль другой, установить `DB_PASSWORD`. При необходимости указать `DB_URL`.

Открыть терминал в корне проекта и запустить:

```bash
./run-local.sh
```

Скрипт подключается к локальному PostgreSQL, создаёт базу `students_db`, если её нет, и запускает приложение. PostgreSQL должен быть уже запущен. Если локальный сервер использует другую схему аутентификации, настройте роль и пароль самостоятельно. В проекте используется стандартная схема `public`.

Сайт: http://localhost:8080

Тестовые учётные записи (только для локальной демонстрации):

| Роль | Email | Пароль |
| --- | --- | --- |
| SUPER_ADMIN | superadmin@example.com | superAdmin |
| ADMIN | admin@example.com | admin |
| TEACHER | teacher@example.com | teacher |

При запуске скрипта `DEMO_DATA_ENABLED=true`; при прямом запуске Spring Boot демоданные по умолчанию отключены. Не включайте демоучётные записи в публичной среде.

## Тесты

```bash
./mvnw test
```

Тесты сервисов используют JUnit и Mockito, PostgreSQL для них не требуется.

## Доступ

ADMIN управляет студентами; TEACHER просматривает студентов; SUPER_ADMIN управляет пользователями. Токен авторизации хранится в памяти вкладки браузера.

## Настройки

`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `DEMO_DATA_ENABLED`, `PORT` задаются переменными окружения. Пример — `.env.example`; файл `.env` автоматически не загружается. В публичной среде обязательно замените JWT-секрет, пароли и отключите демонстрационные данные.
