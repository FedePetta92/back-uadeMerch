package com.uade.e_commerce.dto;

import java.util.List;

import com.uade.e_commerce.model.CarritoItem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class CarritoResponseDTO {
    private Long id;
    private Long usuarioId;
    private Double total;
    private List<CarritoItem> items;
}
