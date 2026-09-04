package com.uade.e_commerce.security;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class CodificadorPassword {
    
    /**
     * Encripta una contraseña usando SHA-256
     * @param password Contraseña en texto plano
     * @return Hash de la contraseña en Base64
     */
    public String encode(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al encriptar la contraseña", e);
        }
    }
    
    /**
     * Verifica si una contraseña en texto plano coincide con su hash
     * @param rawPassword Contraseña en texto plano
     * @param encodedPassword Hash almacenado
     * @return true si coinciden, false en caso contrario
     */

    public boolean matches(String rawPassword, String encodedPassword) {
        String hashedRaw = encode(rawPassword);
        return hashedRaw.equals(encodedPassword);
    }
}
