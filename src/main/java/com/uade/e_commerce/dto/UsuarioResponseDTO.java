package com.uade.e_commerce.dto;

import java.time.LocalDate;

import com.uade.e_commerce.model.UsuarioSexo;

import lombok.Data;

@Data

/** DTO de salida con los datos visibles de un usuario. */
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDate fechaNacimiento;
    private UsuarioSexo sexo;
}