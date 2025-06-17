package com.scratchpad;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new TodoVerticle())
            .onSuccess(id -> System.out.println("TodoVerticle deployed successfully"))
            .onFailure(err -> {
                System.err.println("Failed to deploy TodoVerticle: " + err.getMessage());
                vertx.close();
            });
    }
} 