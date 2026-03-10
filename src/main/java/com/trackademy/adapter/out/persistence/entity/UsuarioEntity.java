package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "email", nullable = false)
    public String email;

    @Column(name = "nombre")
    public String nombre;

    @Column(name = "nombre_preferido")
    public String nombrePreferido;

    @Column(name = "email_institucional")
    public String emailInstitucional;
}
