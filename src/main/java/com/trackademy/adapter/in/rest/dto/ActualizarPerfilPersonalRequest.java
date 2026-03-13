package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.me.ActualizarPerfilPersonalCommand;

public record ActualizarPerfilPersonalRequest(
        String nombre,
        String nombrePreferido,
        String emailInstitucional
) {
    public ActualizarPerfilPersonalCommand toCommand() {
        return new ActualizarPerfilPersonalCommand(nombre, nombrePreferido, emailInstitucional);
    }
}
