package com.example.tradetrackeruser.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
public class HomeController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/hi")
    public String hello() {
        return "hello";
    }

    @GetMapping("/login")
    public String login(){
        return "loginForm";
    }
}
