package com.trackademy.application.port.out;

import com.trackademy.domain.model.Curso;

import java.util.List;
import java.util.Optional;

public interface CursoQueryPort {
    List<Curso> listarCursos();

    Optional<Curso> obtenerPorCodigo(String codigo);
}
