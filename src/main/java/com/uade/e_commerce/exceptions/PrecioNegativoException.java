package com.uade.e_commerce.exceptions;
/** Excepcion de dominio para precios menores que cero. */

public class PrecioNegativoException extends IllegalArgumentException {
    public PrecioNegativoException() {
        super("El precio no puede ser negativo");
    }
}