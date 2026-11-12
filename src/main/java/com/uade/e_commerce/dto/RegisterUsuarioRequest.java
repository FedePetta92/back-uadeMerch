package com.uade.e_commerce.dto;

import java.time.LocalDate;

import com.uade.e_commerce.model.RolUsuario;
import com.uade.e_commerce.model.UsuarioSexo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Datos que manda el front para registrar un usuario.
// Las validaciones se chequean en el controller con @Valid: si alguna falla, la API responde 400
// con el mensaje de cada campo (lo arma GlobalExceptionHandler), en vez de un 500 de la base de datos.
// Los largos máximos coinciden con los de las columnas de la entidad Usuario.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
//TODO: ssanchez - se podría cambiar el nobmre a UsuarioRegisterDTO
// TODO: ssanchez - es buena práctica crear un DTO para request, y otro DTO para response, 2 dto por entidad
public class RegisterUsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 30, message = "El nombre puede tener hasta 30 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 30, message = "El apellido puede tener hasta 30 caracteres")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 50, message = "El email puede tener hasta 50 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

    // Obligatoria: en las bases ya creadas la columna fecha_nacimiento quedó NOT NULL
    // (ddl-auto=update no la relaja), así que un registro sin fecha fallaba en la base.
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento tiene que ser anterior a hoy")
    private LocalDate fechaNacimiento;

    @NotNull(message = "El sexo es obligatorio")
    private UsuarioSexo sexo;

    // Opcional: si no viene, el usuario se crea como USER.
    // Se deja así para poder crear un ADMIN de prueba desde Postman y mostrar el rol en la demo.
    private RolUsuario rol;
}
