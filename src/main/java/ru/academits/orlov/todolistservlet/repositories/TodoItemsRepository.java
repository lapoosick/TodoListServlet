package ru.academits.orlov.todolistservlet.repositories;

import ru.academits.orlov.todolistservlet.entities.TodoItem;

import java.util.List;

public interface TodoItemsRepository {
    List<TodoItem> getAll();

    void create(String text);

    void update(int id, String text);

    void delete(int id);
}
