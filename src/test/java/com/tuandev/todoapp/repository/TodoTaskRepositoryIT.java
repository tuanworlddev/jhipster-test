package com.tuandev.todoapp.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tuandev.todoapp.IntegrationTest;
import com.tuandev.todoapp.domain.TodoTask;
import com.tuandev.todoapp.domain.User;
import com.tuandev.todoapp.domain.enumeration.TaskStatus;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
class TodoTaskRepositoryIT {

    @Autowired
    private TodoTaskRepository todoTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void findAllByOwnerLoginOrderByCreatedDateDescReturnsOnlyOwnedTasks() {
        User owner = createUser("repo-owner", "repo-owner@example.com");
        User anotherOwner = createUser("repo-another-owner", "repo-another-owner@example.com");

        TodoTask olderTask = createTask(owner, "Older task", TaskStatus.PENDING, Instant.now().minusSeconds(120));
        TodoTask newerTask = createTask(owner, "Newer task", TaskStatus.IN_PROGRESS, Instant.now().minusSeconds(60));
        createTask(anotherOwner, "Other owner task", TaskStatus.COMPLETED, Instant.now());

        List<TodoTask> tasks = todoTaskRepository.findAllByOwnerLoginOrderByCreatedDateDesc(owner.getLogin());

        assertThat(tasks).extracting(TodoTask::getTitle).containsExactlyInAnyOrder(newerTask.getTitle(), olderTask.getTitle());
        assertThat(tasks).allMatch(task -> task.getOwner().getId().equals(owner.getId()));
    }

    private User createUser(String login, String email) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K");
        user.setActivated(true);
        return userRepository.saveAndFlush(user);
    }

    private TodoTask createTask(User owner, String title, TaskStatus status, Instant createdDate) {
        TodoTask task = new TodoTask();
        task.setOwner(owner);
        task.setTitle(title);
        task.setDescription(title + " description");
        task.setStatus(status);
        task.setCreatedBy(owner.getLogin());
        task.setCreatedDate(createdDate);
        task.setLastModifiedBy(owner.getLogin());
        task.setLastModifiedDate(createdDate);
        return todoTaskRepository.saveAndFlush(task);
    }
}
