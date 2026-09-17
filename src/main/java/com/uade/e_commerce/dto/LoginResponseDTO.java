package com.uade.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

/** DTO de respuesta que contiene el resultado y el token de autenticacion. */
public class LoginResponseDTO {

    private String token;

}
