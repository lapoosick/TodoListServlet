package ru.academits.orlov.todolistservlet.repositories;

import ru.academits.orlov.todolistservlet.entities.TodoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TodoItemsInMemoryRepository implements TodoItemsRepository {
    private static final List<TodoItem> todoItems = new ArrayList<>();
    private static final AtomicInteger newId = new AtomicInteger();

    @Override
    public List<TodoItem> getAll() {
        synchronized (todoItems) {
            return todoItems.stream()
                    .map(TodoItem::new)
                    .toList();
        }
    }

    @Override
    public void create(TodoItem item) {
        synchronized (todoItems) {
            item.setId(newId.incrementAndGet());
            todoItems.add(item);
        }
    }

    @Override
    public void update(TodoItem item) {
        synchronized (todoItems) {
            int todoItemId = item.getId();

            TodoItem repositoryItem = todoItems.stream()
                    .filter(todoItem -> todoItem.getId() == todoItemId)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Can't find item with id = " + todoItemId));

            repositoryItem.setText(item.getText());
        }
    }

    @Override
    public void delete(int itemId) {
        synchronized (todoItems) {
            todoItems.removeIf(item -> item.getId() == itemId);
        }
    }
}
