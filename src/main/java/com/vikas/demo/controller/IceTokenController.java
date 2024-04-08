package com.vikas.demo.controller;

import com.vikas.demo.dto.SendIceDTO;
import com.vikas.demo.service.IceTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class IceTokenController {

    private final IceTokenService iceTokenService;

    @PostMapping("/user/{userId}/iceToken/send")
    public ResponseEntity<String> sendIceToken(@PathVariable(name = "userId") Long userId,
                                               @RequestBody SendIceDTO sendIceDTO) {

        String message = iceTokenService.sendIceToken(userId, sendIceDTO);
        return new ResponseEntity<>(message, HttpStatus.CREATED);

    }

}
