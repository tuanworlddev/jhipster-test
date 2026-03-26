package com.tuandev.todoapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tuandev.todoapp.IntegrationTest;
import com.tuandev.todoapp.domain.TodoTask;
import com.tuandev.todoapp.domain.User;
import com.tuandev.todoapp.domain.enumeration.TaskStatus;
import com.tuandev.todoapp.repository.TodoTaskRepository;
import com.tuandev.todoapp.repository.UserRepository;
import com.tuandev.todoapp.web.vm.TodoTaskForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@IntegrationTest
class TodoTaskServiceIT {

    @Autowired
    private TodoTaskService todoTaskService;

    @Autowired
    private TodoTaskRepository todoTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    @WithMockUser("service-owner")
    void createTaskAssignsCurrentUserAndPendingStatus() {
        createUser("service-owner", "service-owner@example.com");

        TodoTaskForm form = new TodoTaskForm();
        form.setTitle("Create service task");
        form.setDescription("Service description");

        TodoTask task = todoTaskService.createTask(form);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(task.getOwner().getLogin()).isEqualTo("service-owner");
    }

    @Test
    @Transactional
    @WithMockUser("service-intruder")
    void updateStatusRejectsTasksOwnedByAnotherUser() {
        User owner = createUser("service-owner-2", "service-owner-2@example.com");
        createUser("service-intruder", "service-intruder@example.com");
        TodoTask task = createTask(owner, "Protected task");

        assertThatThrownBy(() -> todoTaskService.updateStatus(task.getId(), TaskStatus.COMPLETED))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND");
    }

    private User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K");
        user.setActivated(true);
        return userRepository.saveAndFlush(user);
    }

    private TodoTask createTask(User owner, String title) {
        TodoTask task = new TodoTask();
        task.setOwner(owner);
        task.setTitle(title);
        task.setStatus(TaskStatus.PENDING);
        return todoTaskRepository.saveAndFlush(task);
    }
}
