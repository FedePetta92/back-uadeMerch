package com.uade.e_commerce.controller;

import com.uade.e_commerce.dto.LoginRequest;
import com.uade.e_commerce.dto.LoginResponseDTO;
import com.uade.e_commerce.dto.UsuarioResponseDTO;
import com.uade.e_commerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    UsuarioService usuarioService;


    @PostMapping
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequest loginRequest) {

        return ResponseEntity.ok(
                usuarioService.login(loginRequest)
        );

    }




}
