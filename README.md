# KakDela-v2 — Сервис для создания опросов

## Что за проект?
Внутренний конструктор опросов для hh.ru, со входом по аккаунту пользователя hh.ru, гибкой логикой вопросов и нативной интеграцией с экосистемой hh.

## Стек технологий
#### Backend:
- Java 21
- Apache Maven
- Spring Boot 4.0.6 (Web, Data, Security, Email)
- Apache Tomcat 
- PostgreSQL
- SeaweedFS
#### Frontend:
- React
- Redux Toolkit
- @hh.ru/magrite-ui
- Axios

## Готовые образы
* [DockerHub](https://hub.docker.com/repository/docker/kakdelav2/kakdela.v2)

## Инструкции по запуску
Приложение поднимается одной командой в Docker
#### dev-версия (подробные логи, дополнительные контейнеры с pgAdmin4 и MailHog, используется `.env`):
```
docker compose -f docker-compose.yml up --build
```
#### prod-версия для локального запуска (только error логи, образы подтягиваются с GitHub Packages репозитория, пути всех необходимых файлов прописаны по структуре репозитория, используется `.env.prod`):
```
docker compose -f docker-compose.prod.local.yml up --build
```
#### prod-версия для запуска на сервере (только error логи, образы подтягиваются с приватного Docker Hub, все необходимые файлы должны лежать на одном уровне с docker-compose файлом, файл с переменными окружения должен называться `.env` — по значениям параметров должен совпадать c `.env.prod`):
```
docker compose -f docker-compose.prod.yml up --build
```