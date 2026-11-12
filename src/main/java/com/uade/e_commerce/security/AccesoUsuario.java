package com.uade.e_commerce.security;

import com.uade.e_commerce.exceptions.AccesoDenegadoException;
import com.uade.e_commerce.model.Usuario;

// Regla de autorizacion: un usuario solo puede operar sobre sus propios datos.
// Compara el id del usuario autenticado (el del token JWT) con el id que viene en la URL.
public final class AccesoUsuario {

    private AccesoUsuario() {
    }

    public static void validarMismoUsuario(Usuario usuarioLogueado, Long usuarioId) {
        if (usuarioLogueado == null || !usuarioLogueado.getId().equals(usuarioId)) {
            throw new AccesoDenegadoException("No tiene permiso para acceder a los datos de otro usuario");
        }
    }
}
