package com.trackademy.application.port.in;

import com.trackademy.domain.model.Curso;

import java.util.List;
import java.util.Optional;

public interface CatalogoCursosUseCase {
    List<Curso> listarCursos();

    Optional<Curso> obtenerPorCodigo(String codigo);
}
