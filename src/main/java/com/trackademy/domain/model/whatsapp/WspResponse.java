package com.trackademy.domain.model.whatsapp;

import java.util.List;

public sealed interface WspResponse {

    record Texto(String body) implements WspResponse {}

    record Botones(String body, List<Boton> botones) implements WspResponse {
        public record Boton(String id, String etiqueta) {}
    }

    record Lista(String body, String botonAbrir, List<Seccion> secciones) implements WspResponse {
        public record Seccion(String titulo, List<Item> items) {}
        public record Item(String id, String titulo, String descripcion) {}
    }
}
