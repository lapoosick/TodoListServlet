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

        String baseUrl = getServletContext().getContextPath() + "/";

        HttpSession session = req.getSession(false);
        String emptyCreateItemErrorHtml = "";
        StringBuilder todoListHtml = new StringBuilder();

        if (session != null) {
            String emptyCreateItemError = (String) session.getAttribute("emptyCreateItem");

            if (emptyCreateItemError != null) {
                emptyCreateItemErrorHtml = "<div>%s</div>".formatted(StringEscapeUtils.escapeHtml4(emptyCreateItemError));

                session.removeAttribute("emptyCreateItem");
            }

            TodoItemsRepository repository = new TodoItemsInMemoryRepository();
            List<TodoItem> todoItems = repository.getAll();

            Integer emptyUpdateItemId = (Integer) session.getAttribute("emptyUpdateItemId");
            String emptyUpdateItemError = (String) session.getAttribute("emptyUpdateItem");

            for (TodoItem todoItem : todoItems) {
                int itemId = todoItem.getId();
                String emptyUpdateItemErrorHtml = "";

                if (emptyUpdateItemId != null && emptyUpdateItemId == itemId) {
                    emptyUpdateItemErrorHtml = "<div>%s</div>".formatted(StringEscapeUtils.escapeHtml4(emptyUpdateItemError));

                    session.removeAttribute("emptyUpdateItemId");
                    session.removeAttribute("emptyUpdateItem");
                }

                todoListHtml
                        .append("""
                                <li>
                                    <form action="%s" method="POST">
                                        <input type="text" name="text" value="%s" size=40>
                                        <button type="submit" name="action" value="update">Сохранить</button>
                                        <button type="submit" name="action" value="delete">Удалить</button>
                                        %s
                                        <input type="hidden" name="id" value="%s">
                                    </form>
                                </li>
                                """.formatted(baseUrl, StringEscapeUtils.escapeHtml4(todoItem.getText()),
                                emptyUpdateItemErrorHtml, itemId))
                        .append("\n");
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
                        %s
                    </form>
                
                    <ul>
                        %s
                    </ul>
                </body>
                </html>
                """.formatted(baseUrl, emptyCreateItemErrorHtml, todoListHtml));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");

        if (action != null) {
            TodoItemsRepository todoItemsRepository = new TodoItemsInMemoryRepository();

            switch (action) {
                case "create" -> {
                    if (req.getParameter("text") != null) {
                        String itemText = req.getParameter("text").trim();

                        if (!itemText.isEmpty()) {
                            todoItemsRepository.create(new TodoItem(itemText));
                        } else {
                            HttpSession session = req.getSession();

                            session.setAttribute("emptyCreateItem", "Заметка не может быть пустой.");
                        }
                    }
                }

                case "update" -> {
                    if (req.getParameter("text") != null) {
                        try {
                            int itemId = Integer.parseInt(req.getParameter("id"));
                            String itemText = req.getParameter("text").trim();

                            if (!itemText.isEmpty()) {
                                todoItemsRepository.update(new TodoItem(itemId, itemText));
                            } else {
                                HttpSession session = req.getSession();

                                session.setAttribute("emptyUpdateItemId", itemId);
                                session.setAttribute("emptyUpdateItem", "Заметка не может быть пустой.");
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }

                case "delete" -> {
                    try {
                        int itemId = Integer.parseInt(req.getParameter("id"));

                        todoItemsRepository.delete(itemId);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        resp.sendRedirect(getServletContext().getContextPath() + "/");
    }
}
