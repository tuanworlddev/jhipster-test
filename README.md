# Todo App

Spring Boot + JHipster todo demo with:

- User registration and login
- Personal task board with `Pending`, `In Progress`, and `Completed`
- Inline task creation, edit, move, and delete
- Swagger UI for API testing

## Run locally

Start the app in dev mode:

```bash
./mvnw
```

The app runs on:

```text
http://localhost:8080
```

## Demo URLs

Main pages:

- Home page: `http://localhost:8080/home`
- Login page: `http://localhost:8080/login`
- Register page: `http://localhost:8080/register`

Built-in demo user:

- Username: `user`
- Password: `user`

Default admin user:

- Username: `admin`
- Password: `admin`

## Swagger UI

Swagger UI is available when the `api-docs` profile is enabled.

Run with API docs:

```bash
./mvnw -Dspring.profiles.active=dev,api-docs
```

Swagger URLs:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

## Main endpoints

Authentication and account:

- `POST /api/authenticate`
- `POST /api/register`
- `GET /api/account`

Todo board:

- `GET /home`
- `POST /tasks`
- `POST /tasks/{id}/edit`
- `POST /tasks/{id}/status`
- `POST /tasks/{id}/delete`

## Build

Compile:

```bash
./mvnw -q -DskipTests compile
```

Run tests:

```bash
./mvnw verify
```

Build production jar:

```bash
./mvnw -Pprod clean verify
```
