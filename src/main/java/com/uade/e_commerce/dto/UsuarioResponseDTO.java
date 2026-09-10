package com.uade.e_commerce.dto;

import com.uade.e_commerce.model.UsuarioSexo;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDate fechaNacimiento;
    private UsuarioSexo sexo;
}