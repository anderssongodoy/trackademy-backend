package com.trackademy.application.service;

import com.trackademy.application.port.in.CatalogoCursosUseCase;
import com.trackademy.application.port.out.CursoQueryPort;
import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CatalogoCursosService implements CatalogoCursosUseCase {

    private final CursoQueryPort cursoQueryPort;

    public CatalogoCursosService(CursoQueryPort cursoQueryPort) {
        this.cursoQueryPort = cursoQueryPort;
    }

    @Override
    public List<Curso> listarCursos() {
        return cursoQueryPort.listarCursos();
    }

    @Override
    public List<Curso> listarCursosPorCarrera(Long carreraId) {
        return cursoQueryPort.listarCursosPorCarrera(carreraId);
    }

    @Override
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return cursoQueryPort.obtenerPorCodigo(codigo);
    }

    @Override
    public Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo) {
        return cursoQueryPort.obtenerDetallePorCodigo(codigo);
    }
}
