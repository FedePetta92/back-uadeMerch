package com.uade.e_commerce.dto;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 

/** DTO de entrada usado para crear o modificar un carrito. */
public class CarritoRequestDTO {
    private Long usuarioId;
    private Double total;
    private Long itemId;
    private Integer cantidad;
}
