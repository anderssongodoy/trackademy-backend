package com.trackademy.application.port.out;

import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;

import java.util.List;
import java.util.Optional;

public interface CursoQueryPort {
    List<Curso> listarCursos();

    List<Curso> listarCursosPorCarrera(Long carreraId);

    Optional<Curso> obtenerPorCodigo(String codigo);

    Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo);
}
