package com.uade.e_commerce.service;

import com.uade.e_commerce.service.PagoSimuladoService.ResultadoPago;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class PagoSimuladoServiceTest {

    // No tiene dependencias externas: se instancia directo, sin mocks.
    private final PagoSimuladoService pagoSimuladoService = new PagoSimuladoService();

    // nombre de los test -> metodo_escenario_resultadoEsperado

    @Test
    void procesar_tarjetaComun_devuelveAprobado() {
        ResultadoPago resultado = pagoSimuladoService.procesar("4111111111111111", BigDecimal.valueOf(1000));

        assertThat(resultado.aprobado()).isTrue();
        assertThat(resultado.motivo()).isNull();
    }

    @Test
    void procesar_tarjeta0002_devuelveRechazadaPorTarjeta() {
        ResultadoPago resultado = pagoSimuladoService.procesar("1234567890000002", BigDecimal.valueOf(500));

        assertThat(resultado.aprobado()).isFalse();
        assertThat(resultado.motivo()).isEqualTo("TARJETA_RECHAZADA");
    }

    @Test
    void procesar_tarjeta9995_devuelveRechazadaPorFondos() {
        ResultadoPago resultado = pagoSimuladoService.procesar("1234567890009995", BigDecimal.valueOf(500));

        assertThat(resultado.aprobado()).isFalse();
        assertThat(resultado.motivo()).isEqualTo("FONDOS_INSUFICIENTES");
    }

    @Test
    void procesar_siempreGeneraTransaccionIdConPrefijo() {
        ResultadoPago resultado = pagoSimuladoService.procesar("4111111111111111", BigDecimal.valueOf(100));

        assertThat(resultado.transaccionId()).startsWith("SIM-");
    }
}