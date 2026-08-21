package com.uade.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//TODO: ssanchez - se podría cambiar el nobmre a UsuarioRegisterDTO
// TODO: ssanchez - es buena práctica crear un DTO para request, y otro DTO para response, 2 dto por entidad
public class RegisterRequest {
    private String nombre;
    private String apellido;
    private String email;
    private String password;
}