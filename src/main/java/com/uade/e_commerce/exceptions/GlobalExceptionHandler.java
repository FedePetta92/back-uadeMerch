package com.uade.e_commerce.exceptions;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// Traduce las excepciones a respuestas HTTP con un mensaje en texto plano que el front puede mostrar tal cual.
// Spring elige el handler más específico: RuntimeException queda como último recurso.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(CantidadInvalidaException.class)
    public ResponseEntity<String> handleCantidadInvalida(CantidadInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(OrdenCompraInvalidaException.class)
    public ResponseEntity<String> handleOrdenCompraInvalida(OrdenCompraInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    // Falló alguna validación de un DTO marcado con @Valid (ej: registro sin sexo, email mal escrito).
    // Devuelve 400 con los mensajes de cada campo separados por " | ".
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidacion(MethodArgumentNotValidException ex) {
        String mensajes = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining(" | "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensajes);
    }

    // El cuerpo no se pudo leer: JSON mal armado o un valor que no corresponde al tipo
    // (ej: sexo "" o "X", que no existen en el enum; una fecha con formato inválido).
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleCuerpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Los datos enviados tienen un formato inválido");
    }

    // Un parámetro de la URL no tiene el tipo esperado (ej: /api/productos/abc cuando se espera un número).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Valor inválido para " + ex.getName());
    }

    // La base rechazó el dato (ej: un email repetido que llegó a la vez desde dos pedidos).
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad en la base", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Los datos entran en conflicto con un registro existente");
    }

    // Último recurso: se registra el detalle en el log y al cliente se le manda un mensaje genérico,
    // para no exponer información interna (nombres de tablas, consultas, stack traces).
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleGenerico(RuntimeException ex) {
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocurrió un error inesperado en el servidor");
    }

    @ExceptionHandler(PrecioNegativoException.class)
    public ResponseEntity<String> manejarPrecioNegativo(PrecioNegativoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<String> handleEmailDuplicado(EmailDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<String> handleAccesoDenegado(AccesoDenegadoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler(ProductoNoDisponibleException.class)
    public ResponseEntity<String> handleProductoNoDisponible(ProductoNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
