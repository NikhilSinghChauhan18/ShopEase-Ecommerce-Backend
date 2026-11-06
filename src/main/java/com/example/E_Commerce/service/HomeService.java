package com.example.E_Commerce.service;

import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public String getWelcomeMessage() {
        return "Welcome to ShopEase E-Commerce!";
    }
}
