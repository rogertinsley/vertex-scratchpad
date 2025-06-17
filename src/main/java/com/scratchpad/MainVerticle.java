package com.scratchpad;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
    
    @Override
    public void start(Promise<Void> startPromise) {
        Router mainRouter = Router.router(vertx);
        mainRouter.route().handler(BodyHandler.create());

        // Create and configure TodoVerticle
        TodoVerticle todoVerticle = new TodoVerticle();
        todoVerticle.setRouter(Router.router(vertx));
        
        // Create and configure ProcessVerticle
        ProcessVerticle processVerticle = new ProcessVerticle();
        processVerticle.setRouter(Router.router(vertx));

        // Deploy TodoVerticle
        vertx.deployVerticle(todoVerticle)
            .onSuccess(id -> {
                System.out.println("TodoVerticle deployed successfully");
                
                // Deploy ProcessVerticle
                vertx.deployVerticle(processVerticle)
                    .onSuccess(processId -> {
                        System.out.println("ProcessVerticle deployed successfully");
                        
                        // Mount the routers from both verticles
                        mainRouter.route("/todos/*").subRouter(todoVerticle.getRouter());
                        mainRouter.route("/process/*").subRouter(processVerticle.getRouter());
                        
                        // Start the HTTP server
                        vertx.createHttpServer()
                            .requestHandler(mainRouter)
                            .listen(8888)
                            .onSuccess(http -> {
                                startPromise.complete();
                                System.out.println("HTTP server started on port 8888");
                            })
                            .onFailure(err -> {
                                startPromise.fail(err);
                                System.err.println("Failed to start HTTP server: " + err.getMessage());
                            });
                    })
                    .onFailure(err -> {
                        startPromise.fail(err);
                        System.err.println("Failed to deploy ProcessVerticle: " + err.getMessage());
                    });
            })
            .onFailure(err -> {
                startPromise.fail(err);
                System.err.println("Failed to deploy TodoVerticle: " + err.getMessage());
            });
    }
} 