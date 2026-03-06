package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "usuario_periodo_curso_horario")
public class UsuarioPeriodoCursoHorarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_periodo_curso_id", nullable = false)
    public Long usuarioPeriodoCursoId;

    @Column(name = "bloque_nro", nullable = false)
    public Integer bloqueNro;

    @Column(name = "dia_semana")
    public Short diaSemana;

    @Column(name = "hora_inicio")
    public LocalTime horaInicio;

    @Column(name = "hora_fin")
    public LocalTime horaFin;

    @Column(name = "duracion_min")
    public Short duracionMin;

    @Column(name = "tipo_sesion")
    public String tipoSesion;

    @Column(name = "ubicacion")
    public String ubicacion;

    @Column(name = "url_virtual")
    public String urlVirtual;
}
