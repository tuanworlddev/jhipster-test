package com.tuandev.todoapp.service;

import com.tuandev.todoapp.domain.TodoTask;
import com.tuandev.todoapp.domain.User;
import com.tuandev.todoapp.domain.enumeration.TaskStatus;
import com.tuandev.todoapp.repository.TodoTaskRepository;
import com.tuandev.todoapp.repository.UserRepository;
import com.tuandev.todoapp.security.SecurityUtils;
import com.tuandev.todoapp.service.dto.BoardViewModel;
import com.tuandev.todoapp.web.vm.TodoTaskForm;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service for user-owned todo task operations.
 */
@Service
@Transactional
public class TodoTaskService {

    private final TodoTaskRepository todoTaskRepository;
    private final UserRepository userRepository;

    public TodoTaskService(TodoTaskRepository todoTaskRepository, UserRepository userRepository) {
        this.todoTaskRepository = todoTaskRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BoardViewModel getCurrentUserBoard() {
        String login = getCurrentUserLogin();
        List<TodoTask> tasks = todoTaskRepository.findAllByOwnerLoginOrderByCreatedDateDesc(login);
        Map<TaskStatus, List<TodoTask>> tasksByStatus = new EnumMap<>(TaskStatus.class);
        Arrays.stream(TaskStatus.values()).forEach(status ->
            tasksByStatus.put(
                status,
                tasks
                    .stream()
                    .filter(task -> task.getStatus() == status)
                    .toList()
            )
        );
        return new BoardViewModel(tasksByStatus);
    }

    public TodoTask createTask(TodoTaskForm form) {
        TodoTask todoTask = new TodoTask();
        todoTask.setTitle(form.getTitle().trim());
        todoTask.setDescription(normalizeDescription(form.getDescription()));
        todoTask.setStatus(TaskStatus.PENDING);
        todoTask.setOwner(getCurrentUser());
        return todoTaskRepository.save(todoTask);
    }

    public TodoTask updateTask(Long id, TodoTaskForm form) {
        TodoTask todoTask = getOwnedTask(id);
        todoTask.setTitle(form.getTitle().trim());
        todoTask.setDescription(normalizeDescription(form.getDescription()));
        return todoTaskRepository.save(todoTask);
    }

    public TodoTask updateStatus(Long id, TaskStatus status) {
        TodoTask todoTask = getOwnedTask(id);
        todoTask.setStatus(status);
        return todoTaskRepository.save(todoTask);
    }

    public void deleteTask(Long id) {
        todoTaskRepository.delete(getOwnedTask(id));
    }

    private TodoTask getOwnedTask(Long id) {
        return todoTaskRepository
            .findByIdAndOwnerLogin(id, getCurrentUserLogin())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private User getCurrentUser() {
        return userRepository
            .findOneByLogin(getCurrentUserLogin())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user not found"));
    }

    private String getCurrentUserLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current user login not found")
        );
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
