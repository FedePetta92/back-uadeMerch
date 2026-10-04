package com.uade.e_commerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ordenes_compra")

// Guarda el usuario, el total, el estado, la fecha y los datos del pago simulado (últimos 4 dígitos, id de transacción, motivo de rechazo).

public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Builder.Default
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenCompraItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrdenCompra estado;

    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    // ---- Datos del pago simulado (solo ultimos 4 digitos, nunca la tarjeta completa) ----
    private String metodoPago;
    private String tarjetaUltimos4;
    private String transaccionId;
    private String motivoRechazo;
}