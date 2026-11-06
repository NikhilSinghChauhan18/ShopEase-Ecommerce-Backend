package com.example.E_Commerce.controller;

import com.example.E_Commerce.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final HomeService homeService;

    // Spring automatically injects the HomeService bean through this constructor.
    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/home")
    public String home() {
        return homeService.getWelcomeMessage();
    }
}
