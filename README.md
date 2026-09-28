# Spring Boot Starter

Java 21 · Spring Boot 3.3 · Maven · PostgreSQL · Docker

## Run everything in Docker
```bash
cp .env.example .env      # optional, defaults work
docker compose up --build
```
- http://localhost:8080/api/hello
- http://localhost:8080/api/todos  (GET / POST / PUT /{id} / DELETE /{id})
- http://localhost:8080/actuator/health

## Develop locally (hot iteration in IDE)
Start only the database, run the app from your IDE or Maven:
```bash
docker compose up -d db
mvn spring-boot:run
```

## Tests
```bash
mvn test
```

## Try the API
```bash
curl -X POST localhost:8080/api/todos -H "Content-Type: application/json" -d '{"title":"first todo"}'
curl localhost:8080/api/todos
```

## Layout
```
src/main/java/com/example/app
  AppApplication.java   entry point
  hello/                simple controller
  todo/                 entity + repository + REST controller (sample CRUD)
```
Rename the package/groupId (`com.example`) to your own.
