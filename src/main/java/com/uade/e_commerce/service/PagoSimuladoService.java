

package com.uade.e_commerce.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

//  Simula un flujo de pago.
//  ...0002 -> TARJETA_RECHAZADA
//  ...9995 -> FONDOS_INSUFICIENTES
//  cualquier otra -> APROBADO

// Mira los últimos 4 dígitos de la tarjeta y decide si aprueba o rechaza, sin cobrar nada real.

@Service
public class PagoSimuladoService {

    public record ResultadoPago(boolean aprobado, String transaccionId, String motivo) {}

    public ResultadoPago procesar(String numeroTarjeta, BigDecimal monto) {
        String ultimos4 = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        String transaccionId = "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (ultimos4.equals("0002")) {
            return new ResultadoPago(false, transaccionId, "TARJETA_RECHAZADA");
        }
        if (ultimos4.equals("9995")) {
            return new ResultadoPago(false, transaccionId, "FONDOS_INSUFICIENTES");
        }
        return new ResultadoPago(true, transaccionId, null);
    }
}