package com.trackademy.application.port.in;

import com.trackademy.domain.model.me.ActualizarConfiguracionPeriodoCommand;
import com.trackademy.domain.model.me.ActualizarPerfilAcademicoCommand;
import com.trackademy.domain.model.me.ActualizarPerfilPersonalCommand;
import com.trackademy.domain.model.me.ActualizarDatosCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.MiTarea;
import com.trackademy.domain.model.me.GuardarTareaCommand;
import com.trackademy.domain.model.me.RegistrarNotaEvaluacionCommand;

public interface MeCommandUseCase {

    MiPeriodoActual actualizarConfiguracionPeriodo(String email, ActualizarConfiguracionPeriodoCommand command);

    MiPeriodoActual actualizarPerfilAcademico(String email, ActualizarPerfilAcademicoCommand command);

    MiPeriodoActual actualizarPerfilPersonal(String email, ActualizarPerfilPersonalCommand command);

    MiCurso actualizarDatosCurso(String email, ActualizarDatosCursoCommand command);

    ActualizarHorarioCursoResult actualizarHorarioCurso(String email, ActualizarHorarioCursoCommand command);

    MiEvaluacionCurso registrarNotaEvaluacion(String email, RegistrarNotaEvaluacionCommand command);

    MiTarea crearTarea(String email, GuardarTareaCommand command);

    MiTarea actualizarTarea(String email, Long tareaId, GuardarTareaCommand command);

    void eliminarTarea(String email, Long tareaId);
}
