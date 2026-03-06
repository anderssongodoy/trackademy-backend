package com.trackademy.application.port.out;

import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;

import java.util.List;
import java.util.Optional;

public interface MeQueryPort {
    Optional<MiPeriodoActual> obtenerPeriodoActual(String email);

    List<MiCurso> listarMisCursos(String email);
}
