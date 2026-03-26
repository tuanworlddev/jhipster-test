package com.tuandev.todoapp.web.page;

import com.tuandev.todoapp.config.Constants;
import com.tuandev.todoapp.service.EmailAlreadyUsedException;
import com.tuandev.todoapp.service.JwtTokenService;
import com.tuandev.todoapp.service.MailService;
import com.tuandev.todoapp.service.UserService;
import com.tuandev.todoapp.service.UsernameAlreadyUsedException;
import com.tuandev.todoapp.service.dto.AdminUserDTO;
import com.tuandev.todoapp.web.WebCookieUtils;
import com.tuandev.todoapp.web.vm.WebLoginVM;
import com.tuandev.todoapp.web.vm.WebRegisterVM;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * MVC controller for login and registration pages.
 */
@Controller
public class AuthPageController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final MailService mailService;

    public AuthPageController(
        AuthenticationManagerBuilder authenticationManagerBuilder,
        JwtTokenService jwtTokenService,
        UserService userService,
        MailService mailService
    ) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
        this.mailService = mailService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new WebLoginVM());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
        @Valid @ModelAttribute("loginForm") WebLoginVM loginForm,
        BindingResult bindingResult,
        Model model,
        HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            Authentication authentication = authenticationManagerBuilder
                .getObject()
                .authenticate(new UsernamePasswordAuthenticationToken(loginForm.getUsername(), loginForm.getPassword()));
            String token = jwtTokenService.createToken(authentication, loginForm.isRememberMe());
            WebCookieUtils.addAuthCookie(response, token, jwtTokenService.getTokenValidityInSeconds(loginForm.isRememberMe()));
            return "redirect:/home";
        } catch (BadCredentialsException ex) {
            model.addAttribute("authError", "Invalid username or password.");
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new WebRegisterVM());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("registerForm") WebRegisterVM registerForm,
        BindingResult bindingResult,
        Model model,
        HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin(registerForm.getLogin());
        userDTO.setFirstName(registerForm.getFirstName());
        userDTO.setLastName(registerForm.getLastName());
        userDTO.setEmail(registerForm.getEmail());
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);

        try {
            var user = userService.registerUser(userDTO, registerForm.getPassword());
            mailService.sendActivationEmail(user);

            Authentication authentication = authenticationManagerBuilder
                .getObject()
                .authenticate(new UsernamePasswordAuthenticationToken(registerForm.getLogin(), registerForm.getPassword()));
            String token = jwtTokenService.createToken(authentication, false);
            WebCookieUtils.addAuthCookie(response, token, jwtTokenService.getTokenValidityInSeconds(false));
            return "redirect:/home";
        } catch (UsernameAlreadyUsedException ex) {
            model.addAttribute("registerError", "That username is already in use.");
            return "register";
        } catch (EmailAlreadyUsedException ex) {
            model.addAttribute("registerError", "That email is already in use.");
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        WebCookieUtils.clearAuthCookie(response);
        return "redirect:/login";
    }
}
