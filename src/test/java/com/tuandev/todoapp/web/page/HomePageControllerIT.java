package com.tuandev.todoapp.web.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tuandev.todoapp.IntegrationTest;
import com.tuandev.todoapp.domain.TodoTask;
import com.tuandev.todoapp.domain.User;
import com.tuandev.todoapp.domain.enumeration.TaskStatus;
import com.tuandev.todoapp.repository.TodoTaskRepository;
import com.tuandev.todoapp.repository.UserRepository;
import com.tuandev.todoapp.web.WebCookieUtils;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@IntegrationTest
class HomePageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoTaskRepository todoTaskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void unauthenticatedHomeRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/home").accept(MediaType.TEXT_HTML)).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }

    @Test
    @Transactional
    void registerPageCreatesUserAndStartsAuthenticatedSession() throws Exception {
        mockMvc
            .perform(
                post("/register")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("firstName", "Board")
                    .param("lastName", "User")
                    .param("login", "page-register")
                    .param("email", "page-register@example.com")
                    .param("password", "secret123")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"))
            .andExpect(header().string("Set-Cookie", containsString(WebCookieUtils.AUTH_COOKIE)));

        assertThat(userRepository.findOneByLogin("page-register")).isPresent();
    }

    @Test
    @Transactional
    void authenticatedUserCanCreateUpdateAndDeleteTaskFromHomePage() throws Exception {
        createUser("board-user", "board-user@example.com", "secret123");
        Cookie authCookie = login("board-user", "secret123");

        mockMvc
            .perform(
                post("/tasks")
                    .cookie(authCookie)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "Ship demo")
                    .param("description", "Render three panels on home page")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));

        TodoTask task = todoTaskRepository.findAllByOwnerLoginOrderByCreatedDateDesc("board-user").getFirst();
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);

        mockMvc
            .perform(get("/home").cookie(authCookie).accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Pending")))
            .andExpect(content().string(containsString("In Progress")))
            .andExpect(content().string(containsString("Completed")))
            .andExpect(content().string(containsString("Ship demo")));

        mockMvc
            .perform(
                post("/tasks/{id}/status", task.getId())
                    .cookie(authCookie)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("status", "IN_PROGRESS")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));

        mockMvc
            .perform(
                post("/tasks/{id}/edit", task.getId())
                    .cookie(authCookie)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "Ship polished demo")
                    .param("description", "Updated description")
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));

        TodoTask updatedTask = todoTaskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updatedTask.getTitle()).isEqualTo("Ship polished demo");

        mockMvc
            .perform(post("/tasks/{id}/delete", task.getId()).cookie(authCookie))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"));

        assertThat(todoTaskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    @Transactional
    void authenticatedUserCannotModifyAnotherUsersTask() throws Exception {
        User owner = createUser("board-owner", "board-owner@example.com", "secret123");
        createUser("board-intruder", "board-intruder@example.com", "secret123");
        TodoTask task = new TodoTask();
        task.setOwner(owner);
        task.setTitle("Owner task");
        task.setStatus(TaskStatus.PENDING);
        todoTaskRepository.saveAndFlush(task);
        Cookie authCookie = login("board-intruder", "secret123");

        mockMvc
            .perform(
                post("/tasks/{id}/status", task.getId())
                    .cookie(authCookie)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("status", "COMPLETED")
            )
            .andExpect(status().isNotFound());
    }

    private Cookie login(String username, String password) throws Exception {
        MvcResult result = mockMvc
            .perform(
                post("/login").contentType(MediaType.APPLICATION_FORM_URLENCODED).param("username", username).param("password", password)
            )
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/home"))
            .andReturn();

        return result.getResponse().getCookie(WebCookieUtils.AUTH_COOKIE);
    }

    private User createUser(String login, String email, String password) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setActivated(true);
        return userRepository.saveAndFlush(user);
    }
}
