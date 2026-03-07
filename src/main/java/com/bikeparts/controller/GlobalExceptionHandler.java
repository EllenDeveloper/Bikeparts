package com.bikeparts.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.BindException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public String handleValidationException(BindException ex, Model model) {
        model.addAttribute("errors", ex.getAllErrors());
        model.addAttribute("account", ex.getTarget());
        // TODO: define correct form!

        return "accounts-list";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "my-error";
    }
}
