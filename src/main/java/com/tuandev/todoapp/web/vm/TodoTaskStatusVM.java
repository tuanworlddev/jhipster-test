package com.tuandev.todoapp.web.vm;

import com.tuandev.todoapp.domain.enumeration.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Form data for updating task status.
 */
public class TodoTaskStatusVM {

    @NotNull
    private TaskStatus status;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
