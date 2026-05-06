package com.trackademy.application.port.in;

import com.trackademy.domain.model.radar.AcademicRadar;

public interface AcademicRadarUseCase {

    AcademicRadar obtenerRadar(String email);
}
