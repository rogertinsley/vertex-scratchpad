package com.scratchpad;

import com.scratchpad.handler.ProcessHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.core.DeploymentOptions;

public class ProcessVerticle extends AbstractVerticle {
    private Router router;

    public void setRouter(Router router) {
        this.router = router;
    }

    public Router getRouter() {
        return router;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        DeploymentOptions options = new DeploymentOptions();
        options.setWorkerPoolSize(1);
        vertx.deployVerticle(new ProcessWorkerVerticle(), options)
            .onSuccess(id -> {
                System.out.println("Worker Verticle deployed successfully");
                setupRouter(startPromise);
            })
            .onFailure(err -> startPromise.fail(err));
    }

    private void setupRouter(Promise<Void> startPromise) {
        ProcessHandler processHandler = new ProcessHandler(vertx);
        processHandler.setupRoutes(router);
        startPromise.complete();
    }
} 