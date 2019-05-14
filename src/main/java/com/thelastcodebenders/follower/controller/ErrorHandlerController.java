package com.thelastcodebenders.follower.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorHandlerController implements ErrorController {
    private static final String PATH = "/error";

    @GetMapping(value = PATH)
    public String error() {
        return "not-found-404-error";
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
}
