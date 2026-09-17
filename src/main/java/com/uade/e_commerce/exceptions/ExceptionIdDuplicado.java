package com.uade.e_commerce.exceptions;
/** Excepcion de dominio para identificadores duplicados. */

public class ExceptionIdDuplicado extends RuntimeException {
    public ExceptionIdDuplicado(String message) {
        super(message);
    }

}
