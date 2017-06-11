package com.bankbitcoinow.validator;

import com.bankbitcoinow.controllers.UserController.RegistrationForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import com.bankbitcoinow.services.UserService;

@Component
public class UserValidator implements Validator {
    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> aClass) {
        return RegistrationForm.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        RegistrationForm registrationForm = (RegistrationForm) o;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty", "Email is required");
        if (!errors.hasFieldErrors("email")) {
            if (registrationForm.getEmail().length() < 6 || registrationForm.getEmail().length() > 255) {
                errors.rejectValue("email", "Size.userForm.email", "Email has to be between 6 and 255 characters");
            }
            if (userService.findByEmail(registrationForm.getEmail()) != null) {
                errors.rejectValue("email", "Duplicate.userForm.email", "Email already registered");
            }
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty");
        if (!errors.hasFieldErrors("password")) {
            if (registrationForm.getPassword().length() < 8 || registrationForm.getPassword().length() > 32) {
                errors.rejectValue("password", "Size.userForm.password", "Password has to be betweeen 8 and 32 characters");
            }
        }

//        if (!user.getPasswordConfirm().equals(user.getPassword())) {
//            errors.rejectValue("passwordConfirm", "Diff.userForm.passwordConfirm");
//        }
    }
}
