package com.trackademy.application.service;

import com.trackademy.application.port.in.MeCommandUseCase;
import com.trackademy.application.port.out.MeCommandPort;
import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MeCommandService implements MeCommandUseCase {

    private final MeCommandPort meCommandPort;

    public MeCommandService(MeCommandPort meCommandPort) {
        this.meCommandPort = meCommandPort;
    }

    @Override
    public ActualizarHorarioCursoResult actualizarHorarioCurso(String email, ActualizarHorarioCursoCommand command) {
        return meCommandPort.actualizarHorarioCurso(email, command);
    }
}
