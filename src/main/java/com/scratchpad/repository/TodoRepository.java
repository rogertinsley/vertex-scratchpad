package com.scratchpad.repository;

import com.scratchpad.model.Todo;
import io.vertx.core.Future;
import java.util.List;

public interface TodoRepository {
    Future<List<Todo>> findAll();
    Future<Todo> findById(Long id);
    Future<Todo> create(Todo todo);
    Future<Todo> update(Todo todo);
    Future<Void> delete(Long id);
} 