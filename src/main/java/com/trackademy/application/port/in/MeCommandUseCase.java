package com.trackademy.application.port.in;

import com.trackademy.domain.model.me.ActualizarPerfilAcademicoCommand;
import com.trackademy.domain.model.me.ActualizarDatosCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoCommand;
import com.trackademy.domain.model.me.ActualizarHorarioCursoResult;
import com.trackademy.domain.model.me.MiCurso;
import com.trackademy.domain.model.me.MiEvaluacionCurso;
import com.trackademy.domain.model.me.MiPeriodoActual;
import com.trackademy.domain.model.me.RegistrarNotaEvaluacionCommand;

public interface MeCommandUseCase {

    MiPeriodoActual actualizarPerfilAcademico(String email, ActualizarPerfilAcademicoCommand command);

    MiCurso actualizarDatosCurso(String email, ActualizarDatosCursoCommand command);

    ActualizarHorarioCursoResult actualizarHorarioCurso(String email, ActualizarHorarioCursoCommand command);

    MiEvaluacionCurso registrarNotaEvaluacion(String email, RegistrarNotaEvaluacionCommand command);
}
