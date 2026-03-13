package com.trackademy.adapter.in.rest.dto;

import com.trackademy.domain.model.onboarding.OnboardingPdfPreview;

import java.util.List;

public record OnboardingPdfPreviewResponse(
        String codigoAlumno,
        String nombreCompleto,
        String emailInstitucional,
        Long carreraId,
        String carreraNombre,
        Long campusId,
        String campusNombre,
        String campusTexto,
        Long periodoId,
        String periodoEtiqueta,
        String periodoTexto,
        Integer cicloActual,
        List<CursoDetectadoResponse> cursosDetectados,
        List<String> advertencias
) {
    public static OnboardingPdfPreviewResponse from(OnboardingPdfPreview preview) {
        return new OnboardingPdfPreviewResponse(
                preview.codigoAlumno(),
                preview.nombreCompleto(),
                preview.emailInstitucional(),
                preview.carreraId(),
                preview.carreraNombre(),
                preview.campusId(),
                preview.campusNombre(),
                preview.campusTexto(),
                preview.periodoId(),
                preview.periodoEtiqueta(),
                preview.periodoTexto(),
                preview.cicloActual(),
                preview.cursosDetectados().stream().map(CursoDetectadoResponse::from).toList(),
                preview.advertencias()
        );
    }

    public record CursoDetectadoResponse(
            Long cursoId,
            String codigo,
            String nombre,
            String profesor,
            String seccion,
            String modalidad,
            List<BloqueHorarioResponse> horarios
    ) {
        public static CursoDetectadoResponse from(OnboardingPdfPreview.CursoDetectado curso) {
            return new CursoDetectadoResponse(
                    curso.cursoId(),
                    curso.codigo(),
                    curso.nombre(),
                    curso.profesor(),
                    curso.seccion(),
                    curso.modalidad(),
                    curso.horarios().stream().map(BloqueHorarioResponse::from).toList()
            );
        }
    }

    public record BloqueHorarioResponse(
            Integer diaSemana,
            String horaInicio,
            String horaFin,
            String descripcion
    ) {
        public static BloqueHorarioResponse from(OnboardingPdfPreview.BloqueHorario horario) {
            return new BloqueHorarioResponse(
                    horario.diaSemana(),
                    horario.horaInicio(),
                    horario.horaFin(),
                    horario.descripcion()
            );
        }
    }
}
