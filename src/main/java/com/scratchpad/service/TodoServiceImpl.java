package com.scratchpad.service;

import com.scratchpad.model.Todo;
import com.scratchpad.repository.TodoRepository;
import io.vertx.core.Future;
import java.util.List;

public class TodoServiceImpl implements TodoService {
    private final TodoRepository repository;

    public TodoServiceImpl(TodoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Future<List<Todo>> getAllTodos() {
        return repository.findAll();
    }

    @Override
    public Future<Todo> getTodoById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Future<Todo> createTodo(Todo todo) {
        return repository.create(todo);
    }

    @Override
    public Future<Todo> updateTodo(Todo todo) {
        return repository.findById(todo.getId())
            .compose(existingTodo -> {
                if (existingTodo == null) {
                    return Future.failedFuture("Todo not found");
                }
                return repository.update(todo);
            });
    }

    @Override
    public Future<Void> deleteTodo(Long id) {
        return repository.findById(id)
            .compose(existingTodo -> {
                if (existingTodo == null) {
                    return Future.failedFuture("Todo not found");
                }
                return repository.delete(id);
            });
    }
} 