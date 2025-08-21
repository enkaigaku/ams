package com.ams.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test/hash")
    public String hashPassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        boolean matches = passwordEncoder.matches(password, hash);
        return "Password: " + password + 
               "\nHash: " + hash + 
               "\nMatches: " + matches;
    }
}