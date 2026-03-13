package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.MiDashboardResumen;

import java.util.List;

public record MiDashboardResponse(
        MiPeriodoActualResponse periodoActual,
        Integer semanaActual,
        Integer progresoPeriodoPct,
        long cursosActivos,
        long horariosRegistrados,
        long evaluacionesPendientes,
        long notasRegistradas,
        List<MiEvaluacionCursoResponse> proximasEvaluaciones,
        List<MiCalendarioEventoResponse> proximasSesiones,
        List<MiCalendarioEventoResponse> proximosEventosPeriodo
) {
    public static MiDashboardResponse from(MiDashboardResumen resumen) {
        return new MiDashboardResponse(
                resumen.periodoActual() == null ? null : MiPeriodoActualResponse.from(resumen.periodoActual()),
                resumen.semanaActual(),
                resumen.progresoPeriodoPct(),
                resumen.cursosActivos(),
                resumen.horariosRegistrados(),
                resumen.evaluacionesPendientes(),
                resumen.notasRegistradas(),
                resumen.proximasEvaluaciones().stream().map(MiEvaluacionCursoResponse::from).toList(),
                resumen.proximasSesiones().stream().map(MiCalendarioEventoResponse::from).toList(),
                resumen.proximosEventosPeriodo().stream().map(MiCalendarioEventoResponse::from).toList()
        );
    }
}
