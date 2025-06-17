package com.scratchpad.handler;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.concurrent.ConcurrentHashMap;

public class ProcessHandler {
    private final Vertx vertx;
    private final ConcurrentHashMap<String, JsonObject> dataStore;

    public ProcessHandler(Vertx vertx) {
        this.vertx = vertx;
        this.dataStore = new ConcurrentHashMap<>();
    }

    public void setupRoutes(io.vertx.ext.web.Router router) {
        router.route().handler(BodyHandler.create());
        
        router.get("/").handler(this::handleRoot);
        router.post("/task").handler(this::handleTask);
        router.post("/items").handler(this::handleCreateItem);
        router.get("/items/:id").handler(this::handleGetItem);
        router.get("/items").handler(this::handleGetAllItems);
    }

    private void handleRoot(RoutingContext ctx) {
        JsonObject response = new JsonObject()
            .put("message", "Hello from Process Verticle!")
            .put("timestamp", System.currentTimeMillis());
        sendJsonResponse(ctx, 200, response);
    }

    private void handleTask(RoutingContext ctx) {
        JsonObject task = ctx.body().asJsonObject();
        
        vertx.eventBus().request("worker.task", task)
            .onSuccess(reply -> {
                sendJsonResponse(ctx, 200, new JsonObject(reply.body().toString()));
            })
            .onFailure(err -> {
                sendErrorResponse(ctx, 500, "Task processing failed");
            });
    }

    private void handleCreateItem(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String id = body.getString("id");
        
        if (id == null || id.isEmpty()) {
            sendErrorResponse(ctx, 400, "ID is required");
            return;
        }

        dataStore.put(id, body);
        sendJsonResponse(ctx, 201, body);
    }

    private void handleGetItem(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        JsonObject item = dataStore.get(id);
        
        if (item == null) {
            sendErrorResponse(ctx, 404, "Item not found");
            return;
        }

        sendJsonResponse(ctx, 200, item);
    }

    private void handleGetAllItems(RoutingContext ctx) {
        JsonObject response = new JsonObject()
            .put("items", dataStore.values());
        sendJsonResponse(ctx, 200, response);
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
} 