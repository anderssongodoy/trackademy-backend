package com.trackademy.application.port.in;

import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiHorarioCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;

import java.util.List;
import java.util.Optional;

public interface MeQueryUseCase {

    Optional<MiPeriodoActual> obtenerPeriodoActual(String email);

    List<MiCurso> listarMisCursos(String email);

    List<MiHorarioCurso> listarMisHorarios(String email);

    List<MiEvaluacionCurso> listarMisEvaluaciones(String email, Long cursoId);
}
