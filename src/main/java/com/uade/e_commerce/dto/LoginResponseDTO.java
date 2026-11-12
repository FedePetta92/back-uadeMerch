package com.uade.e_commerce.dto;

import com.uade.e_commerce.model.RolUsuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    // Id del usuario logueado: el front lo necesita para llamar a los endpoints del carrito (/api/carritos/{usuarioId})
    private Long usuarioId;
    // Rol del usuario (USER o ADMIN): el front lo usa para mostrar la pestaña de administración.
    // Es solo para la interfaz: quien decide los permisos es el back, con el rol guardado en la base.
    private RolUsuario rol;

}
