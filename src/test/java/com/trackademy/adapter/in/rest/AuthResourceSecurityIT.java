package com.trackademy.adapter.in.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * Pruebas de integracion para los endpoints de autenticacion (HU-01).
 *
 * Cubre:
 *   - Seguridad (b.2): un POST sin idToken devuelve 400.
 *   - Seguridad (b.2): un POST con idToken vacio devuelve 400.
 *   - Seguridad (b.2): un GET a /session sin header Authorization devuelve "authenticated: false"
 *     (no expone datos del usuario).
 *
 * Test plan: SP-005 (validacion de tokens externos) + SP-006 (proteccion del endpoint de sesion).
 */
@QuarkusTest
class AuthResourceSecurityIT {

    @Test
    @DisplayName("Seguridad: POST /api/v1/auth/google sin idToken responde 400")
    void googleLogin_returnsBadRequestWhenIdTokenMissing() {
        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/v1/auth/google")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Seguridad: POST /api/v1/auth/microsoft con idToken vacio responde 400")
    void microsoftLogin_returnsBadRequestWhenIdTokenBlank() {
        given()
                .contentType("application/json")
                .body("{\"idToken\":\"\"}")
                .when()
                .post("/api/v1/auth/microsoft")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Seguridad: POST /api/v1/auth/google con idToken claramente invalido responde 401")
    void googleLogin_returnsUnauthorizedForBogusIdToken() {
        given()
                .contentType("application/json")
                .body("{\"idToken\":\"este-no-es-un-id-token-valido\"}")
                .when()
                .post("/api/v1/auth/google")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Seguridad: GET /api/v1/auth/session sin Authorization NO expone email ni name")
    void session_doesNotLeakUserWhenNoAuthorizationHeader() {
        given()
                .when()
                .get("/api/v1/auth/session")
                .then()
                .statusCode(200)
                .body("authenticated", org.hamcrest.CoreMatchers.is(false))
                .body("email", org.hamcrest.CoreMatchers.nullValue())
                .body("name", org.hamcrest.CoreMatchers.nullValue());
    }

    @Test
    @DisplayName("Seguridad: GET /api/v1/auth/session con token mal formado devuelve authenticated: false")
    void session_rejectsMalformedToken() {
        given()
                .header("Authorization", "Bearer not-a-real-jwt")
                .when()
                .get("/api/v1/auth/session")
                .then()
                .statusCode(200)
                .body("authenticated", org.hamcrest.CoreMatchers.is(false));
    }
}
