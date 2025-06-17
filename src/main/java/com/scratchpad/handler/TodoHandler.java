package com.scratchpad.handler;

import com.scratchpad.model.Todo;
import com.scratchpad.service.TodoService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class TodoHandler {
    private final TodoService todoService;

    public TodoHandler(TodoService todoService) {
        this.todoService = todoService;
    }

    public void setupRoutes(io.vertx.ext.web.Router router) {
        router.route().handler(BodyHandler.create());
        
        router.get("/").handler(this::getAllTodos);
        router.get("/:id").handler(this::getTodoById);
        router.post("/").handler(this::createTodo);
        router.put("/:id").handler(this::updateTodo);
        router.delete("/:id").handler(this::deleteTodo);
    }

    private void getAllTodos(RoutingContext ctx) {
        todoService.getAllTodos()
            .onSuccess(todos -> {
                JsonObject response = new JsonObject()
                    .put("todos", todos.stream()
                        .map(Todo::toJson)
                        .collect(java.util.stream.Collectors.toList()));
                sendJsonResponse(ctx, 200, response);
            })
            .onFailure(err -> handleError(ctx, err));
    }

    private void getTodoById(RoutingContext ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        todoService.getTodoById(id)
            .onSuccess(todo -> {
                if (todo == null) {
                    sendErrorResponse(ctx, 404, "Todo not found");
                } else {
                    sendJsonResponse(ctx, 200, todo.toJson());
                }
            })
            .onFailure(err -> handleError(ctx, err));
    }

    private void createTodo(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        Todo todo = Todo.fromJson(body);
        todoService.createTodo(todo)
            .onSuccess(createdTodo -> {
                sendJsonResponse(ctx, 201, createdTodo.toJson());
            })
            .onFailure(err -> handleError(ctx, err));
    }

    private void updateTodo(RoutingContext ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        JsonObject body = ctx.body().asJsonObject();
        Todo todo = Todo.fromJson(body);
        todo.setId(id);
        todoService.updateTodo(todo)
            .onSuccess(updatedTodo -> {
                sendJsonResponse(ctx, 200, updatedTodo.toJson());
            })
            .onFailure(err -> handleError(ctx, err));
    }

    private void deleteTodo(RoutingContext ctx) {
        Long id = Long.parseLong(ctx.pathParam("id"));
        todoService.deleteTodo(id)
            .onSuccess(v -> {
                ctx.response()
                    .setStatusCode(204)
                    .end();
            })
            .onFailure(err -> handleError(ctx, err));
    }

    private void sendJsonResponse(RoutingContext ctx, int statusCode, JsonObject body) {
        ctx.response()
            .setStatusCode(statusCode)
            .putHeader("content-type", "application/json")
            .end(body.encode());
    }

    private void sendErrorResponse(RoutingContext ctx, int statusCode, String message) {
        sendJsonResponse(ctx, statusCode, new JsonObject().put("error", message));
    }

    private void handleError(RoutingContext ctx, Throwable err) {
        sendErrorResponse(ctx, 500, err.getMessage());
    }
} 