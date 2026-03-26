package com.tuandev.todoapp.service.dto;

import com.tuandev.todoapp.domain.TodoTask;
import com.tuandev.todoapp.domain.enumeration.TaskStatus;
import java.util.List;
import java.util.Map;

/**
 * View model for rendering the task board.
 */
public record BoardViewModel(Map<TaskStatus, List<TodoTask>> tasksByStatus) {
    public List<TodoTask> getTasks(TaskStatus status) {
        return tasksByStatus.getOrDefault(status, List.of());
    }
}
