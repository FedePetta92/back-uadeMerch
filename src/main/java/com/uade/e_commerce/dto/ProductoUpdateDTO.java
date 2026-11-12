package com.uade.e_commerce.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Datos para editar un producto desde el panel de administración.
// descripcion e imagen son opcionales: si no vienen (null), se conserva el valor que ya tenía el producto.
@Data
public class ProductoUpdateDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre puede tener hasta 30 caracteres")
    private String nombre;

    @Size(max = 100, message = "La descripción puede tener hasta 100 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0", message = "El precio no puede ser negativo")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Size(max = 255, message = "La ruta de la imagen puede tener hasta 255 caracteres")
    private String imagen;
}
