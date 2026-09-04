package com.uade.e_commerce.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductoUpdateDTO {
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
}
