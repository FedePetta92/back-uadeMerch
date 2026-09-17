package com.uade.e_commerce.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data

/** DTO de entrada para actualizar parcialmente un producto. */
public class ProductoUpdateDTO {
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
}
