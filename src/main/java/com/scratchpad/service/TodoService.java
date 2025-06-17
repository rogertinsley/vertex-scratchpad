package com.scratchpad.service;

import com.scratchpad.model.Todo;
import io.vertx.core.Future;
import java.util.List;

public interface TodoService {
    Future<List<Todo>> getAllTodos();
    Future<Todo> getTodoById(Long id);
    Future<Todo> createTodo(Todo todo);
    Future<Todo> updateTodo(Todo todo);
    Future<Void> deleteTodo(Long id);
} 