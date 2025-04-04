package ru.academits.orlov.todolistservlet.repositories;

import ru.academits.orlov.todolistservlet.entities.TodoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TodoItemsInMemoryRepository implements TodoItemsRepository {
    private static final List<TodoItem> TODO_ITEMS = new ArrayList<>();
    private static final AtomicInteger NEW_ID = new AtomicInteger();

    @Override
    public List<TodoItem> getAll() {
        synchronized (TODO_ITEMS) {
            return TODO_ITEMS.stream()
                    .map(TodoItem::new)
                    .toList();
        }
    }

    @Override
    public void create(String text) {
        synchronized (TODO_ITEMS) {
            TODO_ITEMS.add(new TodoItem(NEW_ID.incrementAndGet(), text));
        }
    }

    @Override
    public void update(int id, String text) {
        synchronized (TODO_ITEMS) {
            TodoItem repositoryItem = TODO_ITEMS.stream()
                    .filter(todoItem -> todoItem.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Can't find item with id = " + id));

            repositoryItem.setText(text);
        }
    }

    @Override
    public void delete(int itemId) {
        synchronized (TODO_ITEMS) {
            TODO_ITEMS.removeIf(item -> item.getId() == itemId);
        }
    }
}
