package com.bankbitcoinow.controllers;

import com.bankbitcoinow.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.bankbitcoinow.services.SecurityService;
import com.bankbitcoinow.services.UserService;
import com.bankbitcoinow.validator.UserValidator;

import java.sql.Timestamp;

@RestController
public class UserController {

    public static class RegistrationForm {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public Object registration(@RequestBody RegistrationForm registrationForm, BindingResult bindingResult) throws BindException {
        userValidator.validate(registrationForm, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }

        User user = new User();
        user.setEmail(registrationForm.getEmail());
        user.setPassword(registrationForm.getPassword());
        user.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        userService.save(user);

        //securityService.autologin(registrationForm.getUsername(), registrationForm.getPassword());

        return user;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model, String error, String logout) {
        if (error != null)
            model.addAttribute("error", "Your email and password is invalid.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "login";
    }

    @RequestMapping(value = {"/", "/welcome"}, method = RequestMethod.GET)
    public String welcome(Model model) {
        return "welcome";
    }
}
