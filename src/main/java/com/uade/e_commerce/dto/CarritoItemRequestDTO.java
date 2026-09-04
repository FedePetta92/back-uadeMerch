package com.uade.e_commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemRequestDTO {

    private Long productoId;
    private Integer cantidad;
}
