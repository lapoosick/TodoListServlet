package ru.academits.orlov.todolistservlet.servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.text.StringEscapeUtils;
import ru.academits.orlov.todolistservlet.entities.TodoItem;
import ru.academits.orlov.todolistservlet.repositories.TodoItemsInMemoryRepository;
import ru.academits.orlov.todolistservlet.repositories.TodoItemsRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.util.List;

@WebServlet("")
public class TodoListServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html");

        PrintWriter writer = resp.getWriter();

        String baseURL = getServletContext().getContextPath() + "/";

        HttpSession session = req.getSession(false);
        String emptyCreateTodoItemErrorHtml = "";
        StringBuilder todoListHtml = new StringBuilder();

        if (session != null) {
            String emptyCreateTodoItemError = (String) session.getAttribute("emptyCreateTodoItem");

            if (emptyCreateTodoItemError != null) {
                emptyCreateTodoItemErrorHtml = "<div>%s</div>".formatted(StringEscapeUtils.escapeHtml4(emptyCreateTodoItemError));

                session.removeAttribute("emptyCreateTodoItem");
            }

            TodoItemsRepository repository = new TodoItemsInMemoryRepository();
            List<TodoItem> todoItems = repository.getAll();

            Integer emptyTodoItemId = (Integer) session.getAttribute("emptyTodoItemId");
            String emptyUpdateTodoItemErrorHtml = "";
            String invalidIdErrorHtml = "";

            for (TodoItem todoItem : todoItems) {
                int todoItemId = todoItem.getId();

                if (emptyTodoItemId != null && emptyTodoItemId == todoItemId) {
                    String emptyUpdateTodoItemError = (String) session.getAttribute("emptyUpdateTodoItem");
                    emptyUpdateTodoItemErrorHtml = "<div>%s</div>".formatted(StringEscapeUtils.escapeHtml4(emptyUpdateTodoItemError));

                    session.removeAttribute("emptyTodoItemId");
                    session.removeAttribute("emptyUpdateTodoItem");
                }

                String invalidIdError = (String) session.getAttribute("invalidId");

                if (invalidIdError != null) {
                    invalidIdErrorHtml = "<div>%s</div>".formatted(StringEscapeUtils.escapeHtml4(invalidIdError));

                    session.removeAttribute("invalidId");
                }

                todoListHtml
                        .append("""
                                <li>
                                    <form action="%s" method="POST">
                                        <input type="text" name="text" value="%s" size=40>
                                        <button type="submit" name="action" value="update">Сохранить</button>
                                        <button type="submit" name="action" value="delete">Удалить</button>
                                        %s
                                        %s
                                        <input type="hidden" name="id" value="%s">
                                    </form>
                                </li>
                                """.formatted(baseURL, StringEscapeUtils.escapeHtml4(todoItem.getText()),
                                emptyUpdateTodoItemErrorHtml, invalidIdErrorHtml, todoItemId))
                        .append("\n");

                emptyUpdateTodoItemErrorHtml = "";
                invalidIdErrorHtml = "";
            }
        }

        writer.println("""
                <!DOCTYPE html>
                <html lang="ru">
                <head>
                    <meta charset="UTF-8">
                    <title>TODO List Servlets</title>
                </head>
                <body>
                    <h1>Список дел</h1>
                
                    <form action="%s" method="POST">
                        <input type="text" name="text" size=40>
                        <button type="submit" name="action" value="create">Добавить</button>
                        <button type="submit" name="action" value="foo">Bad button</button>
                        %s
                    </form>
                
                    <ul>
                        %s
                    </ul>
                </body>
                </html>
                """.formatted(baseURL, emptyCreateTodoItemErrorHtml, todoListHtml));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IllegalArgumentException, IOException {
        String action = req.getParameter("action");
        TodoItemsRepository todoItemsRepository = new TodoItemsInMemoryRepository();

        switch (action) {
            case "create" -> {
                String text = req.getParameter("text").trim();

                if (text.isEmpty()) {
                    HttpSession session = req.getSession();
                    session.setAttribute("emptyCreateTodoItem", "Заметка не может быть пустой.");
                } else {
                    todoItemsRepository.create(text);
                }
            }

            case "update" -> {
                int todoItemId = Integer.parseInt(req.getParameter("id"));
                String text = req.getParameter("text").trim();
                HttpSession session = req.getSession();

                if (text.isEmpty()) {
                    session.setAttribute("emptyTodoItemId", todoItemId);
                    session.setAttribute("emptyUpdateTodoItem", "Заметка не может быть пустой.");
                } else {
                    try {
                        todoItemsRepository.update(todoItemId, text);
                    } catch (IllegalArgumentException e) {
                        session.setAttribute("invalidId", "Невозможно найти запись с указанным id.");
                    }
                }
            }

            case "delete" -> {
                int id = Integer.parseInt(req.getParameter("id"));

                todoItemsRepository.delete(id);
            }
        }

        String baseURL = getServletContext().getContextPath() + "/";
        resp.sendRedirect(baseURL);
    }
}
