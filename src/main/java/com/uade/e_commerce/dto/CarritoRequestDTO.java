package com.uade.e_commerce.dto;

import lombok.Builder;
import lombok.Data;

@Data 
@Builder 
public class CarritoRequest {
    private Long usuarioId;
    private Double total;
    private Long itemId;
    private Integer cantidad;
}
