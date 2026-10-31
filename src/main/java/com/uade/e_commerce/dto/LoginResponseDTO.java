package com.uade.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    // Id del usuario logueado: el front lo necesita para llamar a los endpoints del carrito (/api/carritos/{usuarioId})
    private Long usuarioId;

}
