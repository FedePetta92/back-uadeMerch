package com.uade.e_commerce.dto;

import lombok.Data;

@Data
/** DTO de entrada para actualizar los datos de un usuario. */
public class UsuarioUpdateDTO {
    private String nombre;
    private String apellido;
}
