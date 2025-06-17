# Vertx Scratchpad

Hacking and scratching with vertx - https://vertx.io

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

## Quick Start with Docker Compose

The easiest way to run the application is using Docker Compose:

```bash
docker compose up
```

This will build and start the application and database. The application will be available at http://localhost:8888

## Testing the Verticles

The application has two main verticles that can be tested independently:

### Todo Verticle

Test the Todo API endpoints:

```bash
# Get all todos
curl http://localhost:8888/todos/

# Create a new todo
curl -X POST http://localhost:8888/todos/ \
  -H "Content-Type: application/json" \
  -d '{"title": "Test Todo", "description": "This is a test todo"}'

# Get a specific todo (replace {id} with actual todo id)
curl http://localhost:8888/todos/{id}

# Update a todo
curl -X PUT http://localhost:8888/todos/{id} \
  -H "Content-Type: application/json" \
  -d '{"title": "Updated Todo", "description": "This is an updated todo", "completed": true}'

# Delete a todo
curl -X DELETE http://localhost:8888/todos/{id}
```

### Process Verticle

Test the Process API endpoints:

```bash
# Get process verticle status
curl http://localhost:8888/process/

# Submit a task for processing
curl -X POST http://localhost:8888/process/task \
  -H "Content-Type: application/json" \
  -d '{"data": "test data"}'

# Create a new item
curl -X POST http://localhost:8888/process/items \
  -H "Content-Type: application/json" \
  -d '{"id": "test1", "data": "test data"}'

# Get a specific item
curl http://localhost:8888/process/items/test1

# Get all items
curl http://localhost:8888/process/items
```

Note: The Process Verticle includes a worker verticle that handles long-running tasks asynchronously. When you submit a task, it will be processed in the background and return a response with the processing status. 