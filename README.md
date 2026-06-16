# KakDela-v2 - Сервис для создания опросов
## Что за проект?
Внутренний конструктор опросов для hh.ru, со входом по аккаунту пользователя hh.ru, гибкой логикой вопросов и нативной интеграцией с экосистемой hh.

## Стек технологий
#### Backend:
- Java 21
- Spring Boot 4.0.6
- Apache Maven
- PostgreSql + Hibernate
- Tomcat
- Lombok
#### Frontend:
- React
- Redux Toolkit
- @hh.ru/magrite-ui
- Axios

## Инструкции по запуску
Приложение поднимается одной командой из под docker
#### dev версия (подробные логи, дополнительный контейнер с pgAdmin4, sprind_ddl_auto=update):
```
docker compose -f docker-compose.yml up --build
```
#### prod версия (только error логи, образы подтягиваются с dockerhub, sprind_ddl_auto=validate):
```
docker compose -f docker-compose.prod.yml up --build
```
