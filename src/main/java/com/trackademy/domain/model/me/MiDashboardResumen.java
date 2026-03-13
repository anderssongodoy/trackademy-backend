package com.trackademy.domain.model.me;

import java.util.List;

public record MiDashboardResumen(
        MiPeriodoActual periodoActual,
        Integer semanaActual,
        Integer progresoPeriodoPct,
        long cursosActivos,
        long horariosRegistrados,
        long evaluacionesPendientes,
        long notasRegistradas,
        List<MiEvaluacionCurso> proximasEvaluaciones,
        List<MiCalendarioEvento> proximasSesiones,
        List<MiCalendarioEvento> proximosEventosPeriodo
) {
}
