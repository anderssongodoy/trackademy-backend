package com.trackademy.domain.model.me;

public record ActualizarPerfilPersonalCommand(
        String nombre,
        String nombrePreferido,
        String emailInstitucional
) {
}
