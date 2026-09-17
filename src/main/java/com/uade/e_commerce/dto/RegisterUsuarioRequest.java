package com.uade.e_commerce.dto;

import java.time.LocalDate;

import com.uade.e_commerce.model.UsuarioSexo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

/** DTO de entrada para registrar un usuario nuevo. */
public class RegisterUsuarioRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private LocalDate fechaNacimiento;
    private UsuarioSexo sexo;
}