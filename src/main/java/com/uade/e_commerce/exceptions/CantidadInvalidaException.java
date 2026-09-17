package com.uade.e_commerce.exceptions;
/** Excepcion de dominio para cantidades ausentes o no permitidas. */

public class CantidadInvalidaException extends RuntimeException {
    public CantidadInvalidaException(String message) {
        super(message);
    }

}
