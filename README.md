# Vertx Scratchpad

Hacking and scratching with vertx - https://vertx.io

[![Build Status](https://github.com/roger/vertx/actions/workflows/maven.yml/badge.svg)](https://github.com/roger/vertx/actions/workflows/maven.yml)

## Prerequisites

- Java 17 or later
- Maven 3.6 or later

## Useful Links

- https://start.vertx.io

## Building the Application

To build the application, run:

```bash
mvn clean package
```

## Running the Application

You can run the application in several ways:

1. Using Maven (development mode):
```bash
mvn compile exec:java
```
This will compile and run the application. For development, you can use this command and restart it when you make changes.

2. Using the generated JAR:
```bash
java -jar target/vertx-app-1.0-SNAPSHOT.jar
```

## Development Workflow

For the best development experience:

1. Run the application in development mode:
```bash
mvn compile exec:java
```

2. Make your changes to the Java files

3. When you want to see your changes, stop the application (Ctrl+C) and run the command again

## Testing the Application

Once the application is running, you can test it by opening a web browser or using curl:

```bash
curl http://localhost:8888/
```

You should see the response: "Hello from Vert.x!" 