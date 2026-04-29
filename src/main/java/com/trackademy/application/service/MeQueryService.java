package com.trackademy.application.service;

import com.trackademy.application.port.in.MeQueryUseCase;
import com.trackademy.application.port.out.MeQueryPort;
import com.trackademy.domain.model.me.MiCalendarioEvento;
import com.trackademy.domain.model.me.MiCalendarSyncAccount;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiDashboardResumen;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiEvaluacionesCursoResumen;
import com.trackademy.domain.model.me.MiHorarioCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.MiRecordatorio;
import com.trackademy.domain.model.me.MiTarea;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MeQueryService implements MeQueryUseCase {

    private final MeQueryPort meQueryPort;

    public MeQueryService(MeQueryPort meQueryPort) {
        this.meQueryPort = meQueryPort;
    }

    @Override
    public Optional<MiPeriodoActual> obtenerPeriodoActual(String email) {
        return meQueryPort.obtenerPeriodoActual(email);
    }

    @Override
    public Optional<MiDashboardResumen> obtenerDashboard(String email) {
        return meQueryPort.obtenerDashboard(email);
    }

    @Override
    public List<MiCurso> listarMisCursos(String email) {
        return meQueryPort.listarMisCursos(email);
    }

    @Override
    public List<MiHorarioCurso> listarMisHorarios(String email) {
        return meQueryPort.listarMisHorarios(email);
    }

    @Override
    public List<MiEvaluacionCurso> listarMisEvaluaciones(String email, Long cursoId) {
        return meQueryPort.listarMisEvaluaciones(email, cursoId);
    }

    @Override
    public MiEvaluacionesCursoResumen obtenerResumenEvaluaciones(String email, Long cursoId) {
        return meQueryPort.obtenerResumenEvaluaciones(email, cursoId);
    }

    @Override
    public List<MiCalendarioEvento> listarCalendario(String email, LocalDate from, LocalDate to) {
        return meQueryPort.listarCalendario(email, from, to);
    }

    @Override
    public List<MiTarea> listarMisTareas(String email) {
        return meQueryPort.listarMisTareas(email);
    }

    @Override
    public List<MiRecordatorio> listarMisRecordatorios(String email, LocalDate from, LocalDate to) {
        return meQueryPort.listarMisRecordatorios(email, from, to);
    }

    @Override
    public List<MiCalendarSyncAccount> listarSincronizacionesCalendario(String email) {
        return meQueryPort.listarSincronizacionesCalendario(email);
    }
}
