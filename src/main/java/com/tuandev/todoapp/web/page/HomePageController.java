package com.tuandev.todoapp.web.page;

import com.tuandev.todoapp.domain.enumeration.TaskStatus;
import com.tuandev.todoapp.service.TodoTaskService;
import com.tuandev.todoapp.web.vm.TodoTaskForm;
import com.tuandev.todoapp.web.vm.TodoTaskStatusVM;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * MVC controller for the authenticated todo board.
 */
@Controller
public class HomePageController {

    private final TodoTaskService todoTaskService;

    public HomePageController(TodoTaskService todoTaskService) {
        this.todoTaskService = todoTaskService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    @GetMapping("/home")
    public String home(Model model) {
        populateBoardModel(model);
        return "home";
    }

    @PostMapping("/tasks")
    public String createTask(@Valid @ModelAttribute("taskForm") TodoTaskForm taskForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            populateBoardModel(model);
            model.addAttribute("taskError", "Please provide a valid title and description.");
            return "home";
        }
        todoTaskService.createTask(taskForm);
        return "redirect:/home";
    }

    @PostMapping("/tasks/{id}/edit")
    public String updateTask(
        @PathVariable Long id,
        @Valid @ModelAttribute("editTaskForm") TodoTaskForm editTaskForm,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateBoardModel(model);
            model.addAttribute("taskError", "Please provide a valid title and description.");
            return "home";
        }
        todoTaskService.updateTask(id, editTaskForm);
        redirectAttributes.addFlashAttribute("message", "Task updated.");
        return "redirect:/home";
    }

    @PostMapping("/tasks/{id}/status")
    public String updateStatus(@PathVariable Long id, @Valid @ModelAttribute TodoTaskStatusVM statusForm) {
        todoTaskService.updateStatus(id, statusForm.getStatus());
        return "redirect:/home";
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoTaskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("message", "Task deleted.");
        return "redirect:/home";
    }

    private void populateBoardModel(Model model) {
        if (!model.containsAttribute("taskForm")) {
            model.addAttribute("taskForm", new TodoTaskForm());
        }
        if (!model.containsAttribute("editTaskForm")) {
            model.addAttribute("editTaskForm", new TodoTaskForm());
        }
        if (!model.containsAttribute("statusForm")) {
            TodoTaskStatusVM statusForm = new TodoTaskStatusVM();
            statusForm.setStatus(TaskStatus.PENDING);
            model.addAttribute("statusForm", statusForm);
        }
        model.addAttribute("board", todoTaskService.getCurrentUserBoard());
        model.addAttribute("statuses", TaskStatus.values());
    }
}
