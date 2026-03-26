package com.tuandev.todoapp.repository;

import com.tuandev.todoapp.domain.TodoTask;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the {@link TodoTask} entity.
 */
@Repository
public interface TodoTaskRepository extends JpaRepository<TodoTask, Long> {
    List<TodoTask> findAllByOwnerLoginOrderByCreatedDateDesc(String ownerLogin);

    Optional<TodoTask> findByIdAndOwnerLogin(Long id, String ownerLogin);
}
