package com.uade.e_commerce.dto;

//import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemResponseDTO {

    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}
