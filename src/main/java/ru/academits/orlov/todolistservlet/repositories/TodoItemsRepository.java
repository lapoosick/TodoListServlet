package ru.academits.orlov.todolistservlet.repositories;

import ru.academits.orlov.todolistservlet.entities.TodoItem;

import java.util.List;

public interface TodoItemsRepository {
    List<TodoItem> getAll();

    void create(TodoItem item);

    void update(TodoItem item);

    void delete(int id);
}
