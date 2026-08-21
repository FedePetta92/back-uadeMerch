package com.uade.e_commerce.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    @GetMapping()
    public String findAllUsuarios() {
        return null;
    }

    @GetMapping("/{id}")
    public String getUsuarioById(@RequestParam Long id) {
        return null;
    }
    
    
}
