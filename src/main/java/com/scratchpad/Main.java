package com.scratchpad;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Main {
    public static void main(String[] args) {
        // Set specific number of event loop threads to 4
        VertxOptions options = new VertxOptions()
                .setEventLoopPoolSize(4);
        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(new MainVerticle())
                .onSuccess(id -> System.out.println("MainVerticle deployed successfully"))
                .onFailure(err -> {
                    System.err.println("Failed to deploy MainVerticle: " + err.getMessage());
                    vertx.close();
                });
    }
}