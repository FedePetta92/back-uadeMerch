package com.uade.e_commerce.exceptions;
/** Excepcion de dominio lanzada cuando el correo ya esta registrado. */

public class EmailDuplicadoException extends RuntimeException {
    public EmailDuplicadoException(String message) {
        super(message);
    }
}