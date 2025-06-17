package com.scratchpad;

import com.scratchpad.model.Todo;
import com.scratchpad.repository.TodoRepository;
import com.scratchpad.repository.TodoRepositoryImpl;
import com.scratchpad.service.TodoService;
import com.scratchpad.service.TodoServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class TodoVerticle extends AbstractVerticle {
    private TodoService todoService;
    private Router router;

    public void setRouter(Router router) {
        this.router = router;
    }

    public Router getRouter() {
        return router;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432")))
            .setHost(System.getenv().getOrDefault("DB_HOST", "localhost"))
            .setDatabase(System.getenv().getOrDefault("DB_NAME", "vertxdb"))
            .setUser(System.getenv().getOrDefault("DB_USER", "vertxuser"))
            .setPassword(System.getenv().getOrDefault("DB_PASSWORD", "vertxpass"));

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(30);

        Pool client = Pool.pool(vertx, connectOptions, poolOptions);

        // Initialize database schema
        client.query("""
            CREATE TABLE IF NOT EXISTS todos (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                completed BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP NOT NULL
            )
        """).execute()
        .onSuccess(result -> {
            System.out.println("Database schema initialized successfully");
            TodoRepository repository = new TodoRepositoryImpl(client);
            todoService = new TodoServiceImpl(repository);
            setupRouter(startPromise);
        })
        .onFailure(err -> {
            System.err.println("Failed to initialize database schema: " + err.getMessage());
            startPromise.fail(err);
        });
    }

    private void setupRouter(Promise<Void> startPromise) {
        router.route().handler(BodyHandler.create());

        router.get("/").handler(ctx -> {
            todoService.getAllTodos()
                .onSuccess(todos -> {
                    JsonObject response = new JsonObject()
                        .put("todos", todos.stream()
                            .map(Todo::toJson)
                            .collect(java.util.stream.Collectors.toList()));
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(response.encode());
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", err.getMessage()).encode());
                });
        });

        router.get("/:id").handler(ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            todoService.getTodoById(id)
                .onSuccess(todo -> {
                    if (todo == null) {
                        ctx.response()
                            .setStatusCode(404)
                            .putHeader("content-type", "application/json")
                            .end(new JsonObject().put("error", "Todo not found").encode());
                    } else {
                        ctx.response()
                            .putHeader("content-type", "application/json")
                            .end(todo.toJson().encode());
                    }
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", err.getMessage()).encode());
                });
        });

        router.post("/").handler(ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            Todo todo = Todo.fromJson(body);
            todoService.createTodo(todo)
                .onSuccess(createdTodo -> {
                    ctx.response()
                        .setStatusCode(201)
                        .putHeader("content-type", "application/json")
                        .end(createdTodo.toJson().encode());
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", err.getMessage()).encode());
                });
        });

        router.put("/:id").handler(ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            JsonObject body = ctx.body().asJsonObject();
            Todo todo = Todo.fromJson(body);
            todo.setId(id);
            todoService.updateTodo(todo)
                .onSuccess(updatedTodo -> {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(updatedTodo.toJson().encode());
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", err.getMessage()).encode());
                });
        });

        router.delete("/:id").handler(ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            todoService.deleteTodo(id)
                .onSuccess(v -> {
                    ctx.response()
                        .setStatusCode(204)
                        .end();
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("error", err.getMessage()).encode());
                });
        });

        startPromise.complete();
    }
} 