package com.scratchpad;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class WorkerVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        // Register a consumer on the event bus
        vertx.eventBus().consumer("worker.task", this::handleTask);
        
        System.out.println("Worker Verticle started");
        startPromise.complete();
    }
    
    private void handleTask(Message<JsonObject> message) {
        // Simulate some long-running task
        try {
            // This is safe to do in a worker verticle
            Thread.sleep(1000);
            
            JsonObject result = new JsonObject()
                .put("status", "completed")
                .put("input", message.body())
                .put("processedAt", System.currentTimeMillis());
                
            message.reply(result);
        } catch (InterruptedException e) {
            message.fail(500, "Task processing failed");
        }
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        System.out.println("Worker Verticle stopped");
        stopPromise.complete();
    }
} 