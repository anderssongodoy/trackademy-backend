package com.trackademy.application.port.in;

import com.trackademy.domain.model.me.MiCalendarioEvento;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiDashboardResumen;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiHorarioCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MeQueryUseCase {

    Optional<MiPeriodoActual> obtenerPeriodoActual(String email);

    Optional<MiDashboardResumen> obtenerDashboard(String email);

    List<MiCurso> listarMisCursos(String email);

    List<MiHorarioCurso> listarMisHorarios(String email);

    List<MiEvaluacionCurso> listarMisEvaluaciones(String email, Long cursoId);

    List<MiCalendarioEvento> listarCalendario(String email, LocalDate from, LocalDate to);
}
