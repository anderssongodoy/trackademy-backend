package com.trackademy.domain.model.onboarding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias del comando de onboarding.
 *
 * Cubre:
 *   - HU-02 (Completar onboarding academico): estructura del comando con todos los pasos
 *     (campus, carrera, periodo, ciclo, cursos, franjas de estudio, confianza por curso).
 *   - HU-03 (Perfil y preferencias): la franja de estudio preferida (dia, hora_inicio, hora_fin)
 *     queda capturada en el mismo onboarding.
 *
 * Test plan: SP-002 (modelo onboarding) y SP-003 (preferencias de estudio).
 */
class OnboardingCommandTest {

    @Test
    @DisplayName("HU-02: el comando de onboarding mantiene todos los pasos del wizard")
    void onboardingCommand_preserves_all_wizard_steps() {
        OnboardingCommand command = new OnboardingCommand(
                "estudiante@trackademy.test",
                "Estudiante Trackademy",
                "Trackademy",
                "estudiante.institucional@upn.pe",
                1L, // campusId
                10L, // periodoId
                5L, // carreraId
                4, // cicloActual
                new BigDecimal("14.50"),
                12,
                List.of(
                        new OnboardingCommand.CursoSeleccionado(
                                100L,
                                "A1",
                                "Profesor Ejemplo",
                                "presencial",
                                List.of(new OnboardingCommand.HorarioCurso(
                                        1, "08:00", "10:00", "teorica", "Aula 201", null
                                ))
                        )
                ),
                List.of(
                        new OnboardingCommand.FranjaEstudioPreferida(
                                1, "18:00", "21:00", 1, "estudio"
                        )
                ),
                List.of(
                        new OnboardingCommand.ConfianzaCurso(100L, 4, "Tengo buena base")
                )
        );

        assertEquals("estudiante@trackademy.test", command.email());
        assertEquals(1L, command.campusId());
        assertEquals(10L, command.periodoId());
        assertEquals(5L, command.carreraId());
        assertEquals(4, command.cicloActual());
        assertEquals(1, command.cursos().size());
        assertEquals(1, command.franjasPreferidasEstudio().size());
        assertEquals(1, command.confianzaPorCurso().size());
    }

    @Test
    @DisplayName("HU-03: la franja preferida de estudio captura dia, hora_inicio y hora_fin")
    void franjaEstudio_capturesDayAndTimeRange() {
        OnboardingCommand.FranjaEstudioPreferida franja = new OnboardingCommand.FranjaEstudioPreferida(
                2,
                "19:00",
                "22:00",
                1,
                "estudio"
        );

        assertEquals(2, franja.diaSemana(), "dia de semana 1..7");
        assertEquals("19:00", franja.horaInicio());
        assertEquals("22:00", franja.horaFin());
        assertEquals("estudio", franja.tipo());

        // Validacion logica: el handler debe rechazar bloques con hora_fin <= hora_inicio
        // (esa validacion se hace en el adapter de persistencia / DTO).
        assertTrue(franja.horaFin().compareTo(franja.horaInicio()) > 0,
                "hora_fin debe ser posterior a hora_inicio");
    }

    @Test
    @DisplayName("HU-02: un curso seleccionado puede no tener horario (modalidad virtual asincrona)")
    void cursoSeleccionado_supportsEmptyHorario() {
        OnboardingCommand.CursoSeleccionado curso = new OnboardingCommand.CursoSeleccionado(
                42L,
                null,
                null,
                "virtual_asincrono",
                List.of()
        );

        assertEquals(42L, curso.cursoId());
        assertNull(curso.seccion());
        assertTrue(curso.horarios().isEmpty(), "se permite curso sin bloques de horario");
    }
}
