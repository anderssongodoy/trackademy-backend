package com.trackademy.application.port.in;

import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;

import java.util.List;
import java.util.Optional;

public interface CatalogoCursosUseCase {
    List<Curso> listarCursos();

    List<Curso> listarCursosPorCarrera(Long carreraId);

    Optional<Curso> obtenerPorCodigo(String codigo);

    Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo);
}
