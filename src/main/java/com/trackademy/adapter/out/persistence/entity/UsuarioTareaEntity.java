package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario_tarea")
public class UsuarioTareaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_periodo_id", nullable = false)
    public Long usuarioPeriodoId;

    @Column(name = "usuario_periodo_curso_id")
    public Long usuarioPeriodoCursoId;

    @Column(name = "titulo", nullable = false)
    public String titulo;

    @Column(name = "descripcion")
    public String descripcion;

    @Column(name = "tipo", nullable = false)
    public String tipo;

    @Column(name = "prioridad", nullable = false)
    public String prioridad;

    @Column(name = "estado", nullable = false)
    public String estado;

    @Column(name = "fecha_vencimiento")
    public OffsetDateTime fechaVencimiento;

    @Column(name = "completed_at")
    public OffsetDateTime completedAt;

    @Column(name = "external_source")
    public String externalSource;

    @Column(name = "external_id")
    public String externalId;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;
}
