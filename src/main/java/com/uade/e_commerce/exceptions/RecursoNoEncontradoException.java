package com.uade.e_commerce.exceptions;
/** Excepcion de dominio para recursos que no existen. */

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }

}
