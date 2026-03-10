package com.trackademy.application.port.in;

import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;

import java.util.List;
import java.util.Optional;

public interface CatalogoCursosUseCase {
    List<Curso> listarCursos();

    List<Curso> listarCursosPorCarrera(Long carreraId);

    List<Curso> buscarCursos(Long carreraId, String query, Integer limit, Integer offset);

    Optional<Curso> obtenerPorCodigo(String codigo);

    Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo);
}
