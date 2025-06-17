package com.scratchpad;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.core.DeploymentOptions;
import java.util.concurrent.ConcurrentHashMap;
import com.scratchpad.model.Todo;
import com.scratchpad.repository.TodoRepository;
import com.scratchpad.repository.TodoRepositoryImpl;
import com.scratchpad.service.TodoService;
import com.scratchpad.service.TodoServiceImpl;

public class MainVerticle extends AbstractVerticle {
    private final ConcurrentHashMap<String, JsonObject> dataStore = new ConcurrentHashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        DeploymentOptions options = new DeploymentOptions();
        options.setWorkerPoolSize(1);
        vertx.deployVerticle(new WorkerVerticle(), options)
            .onSuccess(id -> {
                System.out.println("Worker Verticle deployed successfully");
                setupHttpServer(startPromise);
            })
            .onFailure(err -> startPromise.fail(err));
    }

    private void setupHttpServer(Promise<Void> startPromise) {
        Router router = Router.router(vertx);
        
        router.route().handler(BodyHandler.create());
        router.get("/").handler(ctx -> {
            ctx.response()
                .putHeader("content-type", "application/json")
                .end(new JsonObject()
                    .put("message", "Hello from Vert.x 5.0.0!")
                    .put("timestamp", System.currentTimeMillis())
                    .encode());
        });

        router.post("/process").handler(ctx -> {
            JsonObject task = ctx.body().asJsonObject();
            
            vertx.eventBus().request("worker.task", task)
                .onSuccess(reply -> {
                    ctx.response()
                        .putHeader("content-type", "application/json")
                        .end(reply.body().toString());
                })
                .onFailure(err -> {
                    ctx.response()
                        .setStatusCode(500)
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject()
                            .put("error", "Task processing failed")
                            .encode());
                });
        });

        router.post("/items").handler(ctx -> {
            JsonObject body = ctx.body().asJsonObject();
            String id = body.getString("id");
            
            if (id == null || id.isEmpty()) {
                ctx.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", "ID is required").encode());
                return;
            }

            dataStore.put(id, body);
            ctx.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json")
                .end(body.encode());
        });

        router.get("/items/:id").handler(ctx -> {
            String id = ctx.pathParam("id");
            JsonObject item = dataStore.get(id);
            
            if (item == null) {
                ctx.response()
                    .setStatusCode(404)
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("error", "Item not found").encode());
                return;
            }

            ctx.response()
                .putHeader("content-type", "application/json")
                .end(item.encode());
        });

        router.get("/items").handler(ctx -> {
            JsonObject response = new JsonObject()
                .put("items", dataStore.values());
            
            ctx.response()
                .putHeader("content-type", "application/json")
                .end(response.encode());
        });

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8888)
            .onSuccess(http -> {
                startPromise.complete();
                System.out.println("HTTP server started on port 8888");
            })
            .onFailure(err -> {
                startPromise.fail(err);
                System.err.println("Failed to start HTTP server: " + err.getMessage());
            });
    }
} 