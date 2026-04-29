package com.trackademy.application.service;

import com.trackademy.application.port.in.MeCommandUseCase;
import com.trackademy.application.port.out.MeCommandPort;
import com.trackademy.domain.model.me.ActualizarConfiguracionPeriodoCommand;
import com.trackademy.domain.model.me.ActualizarPerfilAcademicoCommand;
import com.trackademy.domain.model.me.ActualizarPerfilPersonalCommand;
import com.trackademy.domain.model.me.ActualizarDatosCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;
import com.trackademy.domain.model.me.GuardarTareaCommand;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.MiTarea;
import com.trackademy.domain.model.me.RegistrarNotaEvaluacionCommand;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MeCommandService implements MeCommandUseCase {

    private final MeCommandPort meCommandPort;

    public MeCommandService(MeCommandPort meCommandPort) {
        this.meCommandPort = meCommandPort;
    }

    @Override
    public MiPeriodoActual actualizarConfiguracionPeriodo(String email, ActualizarConfiguracionPeriodoCommand command) {
        return meCommandPort.actualizarConfiguracionPeriodo(email, command);
    }

    @Override
    public MiPeriodoActual actualizarPerfilAcademico(String email, ActualizarPerfilAcademicoCommand command) {
        return meCommandPort.actualizarPerfilAcademico(email, command);
    }

    @Override
    public MiPeriodoActual actualizarPerfilPersonal(String email, ActualizarPerfilPersonalCommand command) {
        return meCommandPort.actualizarPerfilPersonal(email, command);
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

    @Override
    public MiTarea crearTarea(String email, GuardarTareaCommand command) {
        return meCommandPort.crearTarea(email, command);
    }

    @Override
    public MiTarea actualizarTarea(String email, Long tareaId, GuardarTareaCommand command) {
        return meCommandPort.actualizarTarea(email, tareaId, command);
    }

    @Override
    public void eliminarTarea(String email, Long tareaId) {
        meCommandPort.eliminarTarea(email, tareaId);
    }
}
