package com.trackademy.application.service;

import com.trackademy.application.port.in.CatalogoCursosUseCase;
import com.trackademy.application.port.out.CursoQueryPort;
import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;
import com.trackademy.domain.model.CursoSilaboDownload;
import com.trackademy.domain.model.CursoSilaboVersion;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public List<Curso> buscarCursos(Long carreraId, String query, Integer limit, Integer offset) {
        int safeLimit = limit == null ? 50 : Math.min(Math.max(limit, 1), 200);
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        String safeQuery = query == null ? null : query.trim();
        if (safeQuery != null && safeQuery.isBlank()) {
            safeQuery = null;
        }
        return cursoQueryPort.buscarCursos(carreraId, safeQuery, safeLimit, safeOffset);
    }

    @Override
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return cursoQueryPort.obtenerPorCodigo(codigo);
    }

    @Override
    public Optional<Curso> obtenerPorPublicId(UUID publicId) {
        return cursoQueryPort.obtenerPorPublicId(publicId);
    }

    @Override
    public Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo) {
        return cursoQueryPort.obtenerDetallePorCodigo(codigo);
    }

    @Override
    public Optional<CursoDetalle> obtenerDetallePorPublicId(UUID publicId) {
        return cursoQueryPort.obtenerDetallePorPublicId(publicId);
    }

    @Override
    public List<CursoSilaboVersion> listarSilabosPorCodigo(String codigo) {
        return cursoQueryPort.listarSilabosPorCodigo(codigo);
    }

    @Override
    public List<CursoSilaboVersion> listarSilabosPorPublicId(UUID publicId) {
        return cursoQueryPort.listarSilabosPorPublicId(publicId);
    }

    @Override
    public Optional<CursoSilaboVersion> obtenerSilaboVigentePorCodigo(String codigo) {
        return cursoQueryPort.obtenerSilaboVigentePorCodigo(codigo);
    }

    @Override
    public Optional<CursoSilaboVersion> obtenerSilaboVigentePorPublicId(UUID publicId) {
        return cursoQueryPort.obtenerSilaboVigentePorPublicId(publicId);
    }

    @Override
    public Optional<CursoSilaboDownload> obtenerSilaboDescarga(Long silaboId) {
        return cursoQueryPort.obtenerSilaboDescarga(silaboId);
    }
}
