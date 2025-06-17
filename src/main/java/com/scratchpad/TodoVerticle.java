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

    @Override
    public void start(Promise<Void> startPromise) {
        // Configure PostgreSQL connection
        PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(5432)
            .setHost("localhost")
            .setDatabase("tododb")
            .setUser("postgres")
            .setPassword("postgres");

        // Pool options
        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(30);

        // Create the client pool
        Pool client = Pool.pool(vertx, connectOptions, poolOptions);

        // Initialize repository and service
        TodoRepository repository = new TodoRepositoryImpl(client);
        todoService = new TodoServiceImpl(repository);

        // Create router
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // Define routes
        router.get("/todos").handler(ctx -> {
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

        router.get("/todos/:id").handler(ctx -> {
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

        router.post("/todos").handler(ctx -> {
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

        router.put("/todos/:id").handler(ctx -> {
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

        router.delete("/todos/:id").handler(ctx -> {
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

        // Start HTTP server
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8888)
            .onSuccess(http -> {
                startPromise.complete();
                System.out.println("Todo HTTP server started on port 8888");
            })
            .onFailure(err -> {
                startPromise.fail(err);
                System.err.println("Failed to start Todo HTTP server: " + err.getMessage());
            });
    }
} 