package com.uade.e_commerce.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder 
public class CarritoResponseDTO {
    private Long id;
    private Long usuarioId;
    private Double total;
}
