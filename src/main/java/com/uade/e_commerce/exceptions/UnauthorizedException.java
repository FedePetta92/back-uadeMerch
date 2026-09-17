package com.uade.e_commerce.exceptions;
/** Excepcion que representa una solicitud sin autorizacion suficiente. */

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}