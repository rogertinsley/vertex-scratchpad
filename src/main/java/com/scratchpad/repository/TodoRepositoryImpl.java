package com.scratchpad.repository;

import com.scratchpad.model.Todo;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TodoRepositoryImpl implements TodoRepository {
    private final Pool client;

    public TodoRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<List<Todo>> findAll() {
        return client.query("SELECT * FROM todos ORDER BY created_at DESC")
            .execute()
            .map(rows -> {
                List<Todo> todos = new ArrayList<>();
                for (Row row : rows) {
                    todos.add(mapRowToTodo(row));
                }
                return todos;
            });
    }

    @Override
    public Future<Todo> findById(Long id) {
        return client.preparedQuery("SELECT * FROM todos WHERE id = $1")
            .execute(Tuple.of(id))
            .map(rows -> {
                if (rows.size() == 0) {
                    return null;
                }
                return mapRowToTodo(rows.iterator().next());
            });
    }

    @Override
    public Future<Todo> create(Todo todo) {
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        
        return client.preparedQuery(
            "INSERT INTO todos (title, description, completed, created_at, updated_at) " +
            "VALUES ($1, $2, $3, $4, $5) RETURNING *")
            .execute(Tuple.of(
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreatedAt(),
                todo.getUpdatedAt()
            ))
            .map(rows -> mapRowToTodo(rows.iterator().next()));
    }

    @Override
    public Future<Todo> update(Todo todo) {
        todo.setUpdatedAt(LocalDateTime.now());
        
        return client.preparedQuery(
            "UPDATE todos SET title = $1, description = $2, completed = $3, updated_at = $4 " +
            "WHERE id = $5 RETURNING *")
            .execute(Tuple.of(
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getUpdatedAt(),
                todo.getId()
            ))
            .map(rows -> {
                if (rows.size() == 0) {
                    return null;
                }
                return mapRowToTodo(rows.iterator().next());
            });
    }

    @Override
    public Future<Void> delete(Long id) {
        return client.preparedQuery("DELETE FROM todos WHERE id = $1")
            .execute(Tuple.of(id))
            .mapEmpty();
    }

    private Todo mapRowToTodo(Row row) {
        Todo todo = new Todo();
        todo.setId(row.getLong("id"));
        todo.setTitle(row.getString("title"));
        todo.setDescription(row.getString("description"));
        todo.setCompleted(row.getBoolean("completed"));
        todo.setCreatedAt(row.getLocalDateTime("created_at"));
        todo.setUpdatedAt(row.getLocalDateTime("updated_at"));
        return todo;
    }
} 