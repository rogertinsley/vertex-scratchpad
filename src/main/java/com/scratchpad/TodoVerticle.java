package com.scratchpad;

import com.scratchpad.config.DatabaseConfig;
import com.scratchpad.handler.TodoHandler;
import com.scratchpad.repository.TodoRepository;
import com.scratchpad.repository.TodoRepositoryImpl;
import com.scratchpad.service.TodoService;
import com.scratchpad.service.TodoServiceImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;

public class TodoVerticle extends AbstractVerticle {
    private TodoService todoService;
    private Router router;

    public void setRouter(Router router) {
        this.router = router;
    }

    public Router getRouter() {
        return router;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        var client = DatabaseConfig.createPool(vertx);

        // Initialize database schema
        client.query("""
            CREATE TABLE IF NOT EXISTS todos (
                id SERIAL PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                completed BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP NOT NULL
            )
        """).execute()
        .onSuccess(result -> {
            System.out.println("Database schema initialized successfully");
            TodoRepository repository = new TodoRepositoryImpl(client);
            todoService = new TodoServiceImpl(repository);
            setupRouter(startPromise);
        })
        .onFailure(err -> {
            System.err.println("Failed to initialize database schema: " + err.getMessage());
            startPromise.fail(err);
        });
    }

    private void setupRouter(Promise<Void> startPromise) {
        TodoHandler todoHandler = new TodoHandler(todoService);
        todoHandler.setupRoutes(router);
        startPromise.complete();
    }
} 