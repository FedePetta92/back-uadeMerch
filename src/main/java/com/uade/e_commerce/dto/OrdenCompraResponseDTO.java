package com.uade.e_commerce.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.uade.e_commerce.model.EstadoOrdenCompra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class OrdenCompraResponseDTO {
    private Long id;
    private Long usuarioId;
    private EstadoOrdenCompra estado;
    private BigDecimal total;
    private LocalDateTime fecha;
    private String metodoPago;
    private String tarjetaUltimos4;
    private String transaccionId;
    private String motivoRechazo;
    private List<OrdenCompraItemResponseDTO> items;
}