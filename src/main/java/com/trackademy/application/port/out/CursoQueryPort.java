package com.trackademy.application.port.out;

import com.trackademy.domain.model.Curso;
import com.trackademy.domain.model.CursoDetalle;
import com.trackademy.domain.model.CursoSilaboDownload;
import com.trackademy.domain.model.CursoSilaboVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CursoQueryPort {
    List<Curso> listarCursos();

    List<Curso> listarCursosPorCarrera(Long carreraId);

    List<Curso> buscarCursos(Long carreraId, String query, Integer limit, Integer offset);

    Optional<Curso> obtenerPorCodigo(String codigo);

    Optional<Curso> obtenerPorPublicId(UUID publicId);

    Optional<CursoDetalle> obtenerDetallePorCodigo(String codigo);

    Optional<CursoDetalle> obtenerDetallePorPublicId(UUID publicId);

    List<CursoSilaboVersion> listarSilabosPorCodigo(String codigo);

    List<CursoSilaboVersion> listarSilabosPorPublicId(UUID publicId);

    Optional<CursoSilaboVersion> obtenerSilaboVigentePorCodigo(String codigo);

    Optional<CursoSilaboVersion> obtenerSilaboVigentePorPublicId(UUID publicId);

    Optional<CursoSilaboDownload> obtenerSilaboDescarga(Long silaboId);
}
