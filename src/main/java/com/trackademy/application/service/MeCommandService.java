package com.trackademy.application.service;

import com.trackademy.application.port.in.MeCommandUseCase;
import com.trackademy.application.port.out.MeCommandPort;
import com.trackademy.domain.model.me.ActualizarPerfilAcademicoCommand;
import com.trackademy.domain.model.me.ActualizarDatosCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.RegistrarNotaEvaluacionCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MeCommandService implements MeCommandUseCase {

    private final MeCommandPort meCommandPort;

    public MeCommandService(MeCommandPort meCommandPort) {
        this.meCommandPort = meCommandPort;
    }

    @Override
    public MiPeriodoActual actualizarPerfilAcademico(String email, ActualizarPerfilAcademicoCommand command) {
        return meCommandPort.actualizarPerfilAcademico(email, command);
    }

    @Override
    public MiCurso actualizarDatosCurso(String email, ActualizarDatosCursoCommand command) {
        return meCommandPort.actualizarDatosCurso(email, command);
    }

    @Override
    public ActualizarHorarioCursoResult actualizarHorarioCurso(String email, ActualizarHorarioCursoCommand command) {
        return meCommandPort.actualizarHorarioCurso(email, command);
    }

    @Override
    public MiEvaluacionCurso registrarNotaEvaluacion(String email, RegistrarNotaEvaluacionCommand command) {
        return meCommandPort.registrarNotaEvaluacion(email, command);
    }
}
