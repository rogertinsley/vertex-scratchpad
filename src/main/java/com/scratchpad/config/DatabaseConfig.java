package com.scratchpad.config;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.core.Vertx;

public class DatabaseConfig {
    public static Pool createPool(Vertx vertx) {
        PgConnectOptions connectOptions = new PgConnectOptions()
            .setPort(Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432")))
            .setHost(System.getenv().getOrDefault("DB_HOST", "localhost"))
            .setDatabase(System.getenv().getOrDefault("DB_NAME", "vertxdb"))
            .setUser(System.getenv().getOrDefault("DB_USER", "vertxuser"))
            .setPassword(System.getenv().getOrDefault("DB_PASSWORD", "vertxpass"));

        PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(30);

        return Pool.pool(vertx, connectOptions, poolOptions);
    }
} 