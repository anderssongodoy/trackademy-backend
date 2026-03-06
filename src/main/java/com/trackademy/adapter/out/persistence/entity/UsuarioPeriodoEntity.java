package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario_periodo")
public class UsuarioPeriodoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "usuario_id", nullable = false)
    public Long usuarioId;

    @Column(name = "periodo_id", nullable = false)
    public Long periodoId;

    @Column(name = "campus_id")
    public Long campusId;

    @Column(name = "carrera_id")
    public Long carreraId;

    @Column(name = "ciclo_actual")
    public Integer cicloActual;

    @Column(name = "meta_promedio_ciclo")
    public BigDecimal metaPromedioCiclo;

    @Column(name = "horas_estudio_semana_objetivo")
    public Integer horasEstudioSemanaObjetivo;

    @Column(name = "onboarding_estado")
    public String onboardingEstado;

    @Column(name = "onboarding_completado_at")
    public OffsetDateTime onboardingCompletadoAt;
}
