package com.uade.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

/** DTO con las credenciales recibidas durante el inicio de sesion. */
public class LoginRequest {
    private String email;
    private String password;
}
