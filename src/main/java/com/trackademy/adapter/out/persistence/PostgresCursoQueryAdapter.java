package com.trackademy.adapter.out.persistence;

import com.trackademy.adapter.out.persistence.entity.CursoEntity;
import com.trackademy.adapter.out.persistence.repository.CursoPanacheRepository;
import com.trackademy.application.port.out.CursoQueryPort;
import com.trackademy.domain.model.Curso;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class PostgresCursoQueryAdapter implements CursoQueryPort {

    private final CursoPanacheRepository repository;

    public PostgresCursoQueryAdapter(CursoPanacheRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Curso> listarCursos() {
        return repository.listarOrdenadosPorCodigo().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return repository.buscarPorCodigo(codigo).map(this::toDomain);
    }

    private Curso toDomain(CursoEntity entity) {
        return new Curso(
                entity.id,
                entity.codigo,
                entity.nombre,
                entity.creditos,
                entity.horasSemanales,
                entity.modalidad
        );
    }
}
