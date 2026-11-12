package com.uade.e_commerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String nombre;

    @Column(nullable = false, length = 30)
    private String apellido;
    
    @Email(message="El formato del correo ingresado no es válido.")
    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'USER'")
    private RolUsuario rol = RolUsuario.USER;

    // NOT NULL, igual que en las bases ya creadas (el registro la exige)
    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private UsuarioSexo sexo;

    // Borrado lógico: en vez de eliminar la fila se marca como dado de baja,
    // así no se pierde el historial (las órdenes de compra lo siguen referenciando).
    // @ColumnDefault hace que las filas que ya existían en la tabla queden activas al agregarse la columna.
    @Builder.Default
    @ColumnDefault("true")
    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;
}