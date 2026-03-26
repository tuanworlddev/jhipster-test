package com.tuandev.todoapp.web.vm;

import jakarta.validation.constraints.NotBlank;

/**
 * View model for the demo login form.
 */
public class WebLoginVM {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private boolean rememberMe;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
