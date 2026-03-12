package com.trackademy.application.service;

import com.trackademy.application.port.in.MeQueryUseCase;
import com.trackademy.application.port.out.MeQueryPort;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiHorarioCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import jakarta.enterprise.context.ApplicationScoped;

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
}
