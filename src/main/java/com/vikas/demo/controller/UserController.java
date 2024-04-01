package com.vikas.demo.controller;

import com.vikas.demo.dto.UserRegistrationDTO;
import com.vikas.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Long> createUser(@RequestBody UserRegistrationDTO userRegistrationDTO) {
        Long id = userService.createUser(userRegistrationDTO);
        return new ResponseEntity<>(id, HttpStatus.CREATED);
    }

}
