package com.trackademy.adapter.out.persistence.repository;

import com.trackademy.adapter.out.persistence.entity.UsuarioEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UsuarioPanacheRepository implements PanacheRepositoryBase<UsuarioEntity, Long> {

    public Optional<UsuarioEntity> buscarPorEmail(String email) {
        return find("lower(email) = ?1", email.toLowerCase()).firstResultOptional();
    }
}
