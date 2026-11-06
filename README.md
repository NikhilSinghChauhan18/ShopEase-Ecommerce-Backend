# ShopEase - E-Commerce (Phase 1)

A beginner-friendly Spring Boot starter project for the ShopEase college project.

## Requirements

- JDK 17 or later
- IntelliJ IDEA
- Internet connection on the first Maven import so Maven can download dependencies

## Open in IntelliJ IDEA

1. Extract the ZIP file.
2. In IntelliJ IDEA, choose **File -> Open**.
3. Select the extracted `ShopEase-Phase1` folder (the folder containing `pom.xml`).
4. When prompted, load the project as a Maven project and allow dependencies to download.
5. Open `src/main/java/com/example/E_Commerce/ECommerceApplication.java`.
6. Click the green Run icon beside the `main` method.

## Test the first API

After the application starts, open a browser or Postman and send:

```http
GET http://localhost:8080/home
```

Expected response:

```text
Welcome to ShopEase E-Commerce!
```

## Project structure

```text
ShopEase-Phase1/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/example/E_Commerce/
    │   │   ├── ECommerceApplication.java
    │   │   ├── controller/
    │   │   │   └── HomeController.java
    │   │   └── service/
    │   │       └── HomeService.java
    │   └── resources/
    │       └── application.properties
    └── test/java/com/example/E_Commerce/
        └── ECommerceApplicationTests.java
```

## Why JPA and MySQL are not included yet

This Phase 1 starter is deliberately configured to run without a database. We will add Spring Data JPA and the MySQL driver in Phase 2 when we configure the database. This avoids datasource startup errors before the database is configured.
