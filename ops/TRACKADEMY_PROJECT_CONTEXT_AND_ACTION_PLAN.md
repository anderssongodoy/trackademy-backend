# Trackademy Project Context And Action Plan

Fecha de referencia: 2026-04-09.

Este documento fija el contexto operativo actual de Trackademy para no perder el criterio de alcance, estado real del proyecto, riesgos y orden de trabajo.

## Alcance real

Trackademy, para efectos de producto, esta compuesto solo por:

- `trackademy-backend`
- `trackademy-frontend`

No contar como producto principal:

- `extraer-pdf`: herramienta o proyecto separado. No es parte del runtime principal.
- `Multilingual Support`: proyecto/carpeta ajena al Trackademy actual.
- `infra`: apoyo de infraestructura, no logica de producto.
- Markdown suelto en la raiz: documentacion o planes, no codigo ejecutable.

Matiz importante:

- La lectura/preview de PDF si existe dentro del backend actual como parte del onboarding.
- Eso no vuelve a `extraer-pdf` parte del producto principal.

## Estado general

Trackademy es un producto academico web con backend Quarkus y frontend Angular.

No es solo un prototipo simple. Tiene backend y frontend conectados con:

- autenticacion
- onboarding
- catalogo academico
- periodo actual
- dashboard
- cursos
- horarios
- calendario
- notas
- perfil
- WhatsApp MVP

Validacion ejecutada:

- Backend: `mvn -DskipTests compile` termino en `BUILD SUCCESS`.
- Frontend: `npm run build` termino correctamente.
- Warning frontend: `onboarding.page.scss` excede el budget por 453 bytes.
- Warning backend: dependencia `quarkus-junit5` reubicada a `quarkus-junit`.

## Backend

El backend no es Spring. Es:

- Quarkus 3.32.2
- Java 21
- Maven
- PostgreSQL
- Hibernate ORM + Panache
- Flyway
- SmallRye OpenAPI
- JWT propio
- validacion de tokens externos Google/Microsoft

Archivo principal de dependencias:

- `trackademy-backend/pom.xml`

Arquitectura:

- `adapter/in/rest`: recursos HTTP y DTOs.
- `application/service`: implementacion de casos de uso.
- `application/port/in`: contratos de entrada.
- `application/port/out`: contratos hacia persistencia o integraciones.
- `adapter/out/persistence`: PostgreSQL/Panache.
- `adapter/out/auth`: Google, Microsoft y JWT.
- `adapter/out/whatsapp`: Meta Graph API.
- `domain/model`: modelos de dominio.

La separacion es buena para MVP y permite evolucionar sin convertir los resources en controladores con logica de negocio.

### Funcionalidad real backend

Implementado:

- Auth Google.
- Auth Microsoft.
- Sesion propia JWT.
- Catalogo de campus, carreras, periodos y cursos.
- Detalle de curso/silabo.
- Onboarding basico.
- Preview de PDF de matricula dentro del onboarding.
- Periodo actual del usuario.
- Dashboard.
- Mis cursos.
- Horarios.
- Calendario mezclando eventos de periodo, evaluaciones y clases.
- Evaluaciones.
- Registro/actualizacion de notas.
- Perfil personal.
- Perfil academico.
- Reconfiguracion del ciclo actual.
- WhatsApp MVP: codigo de vinculacion, estado, desvinculacion, webhook, vinculacion y comandos.

Endpoints clave:

- `GET /health`
- `POST /api/v1/auth/google`
- `POST /api/v1/auth/microsoft`
- `GET /api/v1/auth/session`
- `GET /api/v1/catalog/campuses`
- `GET /api/v1/catalog/carreras`
- `GET /api/v1/catalog/periodos`
- `GET /api/v1/catalog/periodos/{periodoId}/eventos`
- `GET /api/v1/catalog/cursos`
- `GET /api/v1/catalog/cursos/{codigo}`
- `GET /api/v1/catalog/cursos/{codigo}/detalle`
- `POST /api/v1/onboarding/basic`
- `POST /api/v1/onboarding/preview-pdf`
- `GET /api/v1/me/periodo-actual`
- `PUT /api/v1/me/periodo-actual`
- `PUT /api/v1/me/periodo-actual/personal`
- `PUT /api/v1/me/periodo-actual/configuracion`
- `GET /api/v1/me/dashboard`
- `GET /api/v1/me/cursos`
- `PUT /api/v1/me/cursos/{usuarioPeriodoCursoId}`
- `GET /api/v1/me/horarios`
- `PUT /api/v1/me/cursos/{usuarioPeriodoCursoId}/horarios`
- `GET /api/v1/me/evaluaciones`
- `PUT /api/v1/me/cursos/{usuarioPeriodoCursoId}/evaluaciones/{evaluacionCodigo}/nota`
- `GET /api/v1/me/calendario`
- `POST /api/v1/whatsapp/link-code`
- `GET /api/v1/whatsapp/link-status`
- `DELETE /api/v1/whatsapp/link`
- `GET /api/v1/webhooks/whatsapp`
- `POST /api/v1/webhooks/whatsapp`

## Base de datos

Modelo real observado:

- `usuario`
- `usuario_periodo`
- `usuario_periodo_curso`
- `usuario_periodo_curso_horario`
- `usuario_periodo_evaluacion`
- `usuario_preferencia_estudio`
- `usuario_periodo_curso_confianza`
- `curso`
- `campus`
- `carrera`
- `periodo`
- `periodo_evento`
- `silabo`
- `silabo_evaluacion`
- `silabo_unidad`
- `silabo_tema`
- `calendar_sync_account`
- `whatsapp_link_codes`
- `user_whatsapp_links`
- `whatsapp_inbound_message`

Riesgo actual:

- Solo se encontro migracion Flyway para WhatsApp: `V20260409_01__whatsapp_mvp.sql`.
- El resto del schema parece preexistente o creado manualmente.
- `quarkus.flyway.migrate-at-start=false`.
- `quarkus.hibernate-orm.database.generation=none`.

Implicacion:

- El backend espera que la base ya exista.
- Un ambiente nuevo no es reproducible desde cero solo con el repo.
- Esto es una deuda critica si se quiere despliegue serio, staging, CI con base real o recuperacion rapida.

## WhatsApp

WhatsApp no es solo documentacion. Esta implementado en backend y frontend.

Backend:

- Genera codigos `TDK-######`.
- Guarda codigos con expiracion.
- Cancela codigos activos previos del usuario.
- Vincula cuenta por `wa_id`.
- Deduplica mensajes por `meta_message_id`.
- Responde comandos MVP.
- Envia mensajes con Meta Graph API.

Comandos MVP:

- `menu`
- `resumen`
- `pendientes`
- `hoy`
- `cursos`
- `ayuda`

Riesgo:

- Si `app.whatsapp.meta.app-secret` esta vacio, la validacion de firma acepta el webhook.
- Esto puede servir en local, pero debe bloquearse o exigirse en produccion.

## Frontend

Stack:

- Angular 20
- TypeScript
- RxJS
- Angular Router
- Angular Forms
- SCSS
- MSAL Browser

Dominios:

- `identity`: login, sesion, guards, interceptores.
- `academics`: onboarding, dashboard, cursos, horarios, notas, calendario, tareas, recordatorios, perfil, WhatsApp.
- `marketing`: landing.
- `shared`: shell y componentes compartidos.

Rutas principales:

- `/`
- `/auth/sign-in`
- `/auth/callback`
- `/onboarding`
- `/app/dashboard`
- `/app/cursos`
- `/app/cursos/:id`
- `/app/cursos/:id/horario`
- `/app/horario`
- `/app/calendario`
- `/app/notas`
- `/app/tareas`
- `/app/recordatorios`
- `/app/perfil`

Integracion WhatsApp frontend:

- `POST /api/v1/whatsapp/link-code`
- `GET /api/v1/whatsapp/link-status`
- `DELETE /api/v1/whatsapp/link`

Archivo:

- `trackademy-frontend/src/app/domains/academics/infrastructure/api/whatsapp-api.service.ts`

Observacion:

- `tareas` y `recordatorios` hoy parecen derivarse de evaluaciones/calendario existentes.
- No equivalen todavia a tareas manuales o recordatorios persistentes creados por el alumno.

## Riesgos tecnicos principales

1. Migraciones incompletas.
2. Configuracion de produccion con defaults peligrosos si no se sobreescriben.
3. WhatsApp webhook permisivo si falta `app-secret`.
4. Tests casi inexistentes.
5. Manejo de errores no uniforme.
6. `environment.production.ts` tiene `production: false`.
7. CORS esta configurado por propiedades Quarkus y tambien por filtro custom.
8. Documentacion duplicada o repartida entre raiz y `ops`.

## Orden recomendado general

1. Corregir configuracion de produccion y secrets.
2. Corregir `environment.production.ts`.
3. Crear manejo uniforme de errores.
4. Ejecutar QA manual completo del flujo actual.
5. Versionar schema completo con Flyway.
6. Agregar tests backend de endpoints criticos.
7. Endurecer WhatsApp.
8. Despues construir tareas manuales o recordatorios persistentes.

## Prioridad 1: plan detallado

La prioridad 1 busca estabilizar la base antes de agregar funciones nuevas.

Bloques:

- P1.A: configuracion segura de produccion.
- P1.B: manejo uniforme de errores.
- P1.C: estrategia de base de datos y Flyway.

### Progreso P1

Estado al 2026-04-09:

- P1.A iniciado.
- `trackademy-frontend/src/environments/environment.production.ts` fue corregido a `production: true`.
- `trackademy-backend/.env.example` fue reorganizado como contrato local/produccion sin secretos reales.
- `trackademy-backend/src/main/resources/application.properties` ahora expone `app.environment` y `app.whatsapp.enabled`.
- Se agrego `ProductionConfigValidator` para fallar temprano en runtime productivo si falta `APP_AUTH_JWT_SECRET` fuerte.
- La validacion tambien exige configuracion completa de WhatsApp si se activa o si hay variables WhatsApp parcialmente configuradas.
- `.env.prod` no existe localmente porque vive en la VPS; queda pendiente revisar/actualizar esas variables directamente en la VPS antes del proximo deploy.

Validacion ejecutada:

- Backend: `mvn -DskipTests compile` termino en `BUILD SUCCESS`.
- Frontend: `npm run build` termino correctamente con el warning conocido de budget SCSS en onboarding.

### P1.A Configuracion segura de produccion

Prioridad: muy alta.

Trabajo estimado: bajo/medio.

Objetivo:

- Evitar que Trackademy corra en produccion con defaults locales o configuracion ambigua.

Archivos a revisar:

- `trackademy-backend/src/main/resources/application.properties`
- `trackademy-backend/.env.example`
- `trackademy-backend/.env`
- `trackademy-backend/ops/*`
- `trackademy-frontend/src/environments/environment.ts`
- `trackademy-frontend/src/environments/environment.production.ts`

Tareas:

1. Corregir `environment.production.ts`.
   - Cambiar `production: false` a `production: true`.
   - Confirmar `apiBaseUrl`.
   - Confirmar IDs de Google/Microsoft.

2. Revisar variables backend obligatorias para produccion.
   - `QUARKUS_DATASOURCE_JDBC_URL`
   - `QUARKUS_DATASOURCE_USERNAME`
   - `QUARKUS_DATASOURCE_PASSWORD`
   - `APP_AUTH_JWT_SECRET`
   - `APP_AUTH_JWT_ISSUER`
   - `APP_AUTH_MICROSOFT_FRONTEND_CLIENT_ID`
   - `APP_AUTH_GOOGLE_FRONTEND_CLIENT_ID`
   - `APP_WHATSAPP_META_ACCESS_TOKEN`
   - `APP_WHATSAPP_META_PHONE_NUMBER_ID`
   - `APP_WHATSAPP_META_WEBHOOK_VERIFY_TOKEN`
   - `APP_WHATSAPP_META_APP_SECRET`
   - `APP_WHATSAPP_OFFICIAL_NUMBER`

3. Crear validacion de configuracion para produccion.
   - Si se detecta perfil/entorno productivo, fallar temprano si falta secret critico.
   - Minimo obligatorio: JWT secret fuerte.
   - Si WhatsApp esta activo, obligatorio: access token, phone number id, verify token, app secret y numero oficial.

4. Documentar `.env.example`.
   - Separar claramente variables locales vs produccion.
   - Marcar variables obligatorias.
   - No incluir secretos reales.

5. Revisar CORS.
   - Elegir una fuente de verdad: propiedades Quarkus o `CorsFilter`.
   - Para MVP se puede mantener el filtro si esta funcionando, pero documentar que no debe duplicarse sin necesidad.

Criterios de aceptacion:

- Frontend productivo queda con `production: true`.
- Backend no puede arrancar en produccion sin JWT secret fuerte.
- WhatsApp no queda activo en produccion con app secret vacio.
- `.env.example` explica variables obligatorias.
- Build backend y frontend siguen pasando.

Comandos de validacion:

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
mvn -DskipTests compile
```

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-frontend
npm run build
```

### P1.B Manejo uniforme de errores

Prioridad: alta.

Trabajo estimado: medio.

Objetivo:

- Que errores esperados de usuario o dominio devuelvan respuestas coherentes y no 500 genericos.

Problema actual:

- Muchos servicios lanzan `IllegalArgumentException`.
- Algunos resources capturan casos puntuales.
- Falta una capa uniforme para convertir excepciones esperadas en HTTP 400/401/404.

Diseño recomendado:

- Crear DTO de error:

```json
{
  "code": "validation_error",
  "message": "La nota debe estar entre 0 y 20."
}
```

Ubicacion sugerida:

- `adapter/in/rest/dto/ApiErrorResponse.java`
- `adapter/in/rest/mapper/IllegalArgumentExceptionMapper.java`
- Opcional: `adapter/in/rest/mapper/NotFoundExceptionMapper.java`

Tareas:

1. Crear `ApiErrorResponse`.
   - Campos: `code`, `message`.
   - Opcional despues: `details`, `timestamp`, `path`.

2. Crear `ExceptionMapper<IllegalArgumentException>`.
   - Status: 400.
   - Code: `validation_error`.
   - Message: mensaje de la excepcion.

3. Crear excepciones de dominio si hace falta.
   - No obligatorio al inicio.
   - Si se vuelve necesario: `DomainValidationException`, `ResourceNotFoundException`.

4. Revisar resources.
   - Evitar `try/catch` repetidos para validaciones normales.
   - Mantener `401` explicito donde depende de auth.

5. Validar endpoints criticos.
   - Onboarding con datos faltantes.
   - Registrar nota fuera de rango.
   - Actualizar horario invalido.
   - Reconfigurar ciclo sin cursos.
   - Preview PDF sin archivo.

Criterios de aceptacion:

- Errores de validacion devuelven `400` con JSON uniforme.
- Auth invalida sigue devolviendo `401`.
- No se exponen stack traces al cliente.
- Frontend puede mostrar mensajes consistentes.
- Backend compila.

Comandos de validacion:

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
mvn -DskipTests compile
```

### P1.C Estrategia de base de datos y Flyway

Prioridad: muy alta.

Trabajo estimado: medio/alto.

Objetivo:

- Hacer que una base vacia pueda reconstruirse desde migraciones versionadas.

Problema actual:

- Existen entidades para muchas tablas, pero solo se observo una migracion para WhatsApp.
- Si el VPS o una maquina nueva no tiene el schema manual, el backend no puede funcionar.

Enfoque recomendado:

- Primero inventariar schema real.
- Luego crear baseline/migraciones.
- Despues probar base limpia.

Fase 1: inventario.

Tareas:

1. Listar todas las entidades JPA.
2. Mapear tabla y columnas de cada entidad.
3. Comparar con la base real si esta disponible.
4. Identificar indices, unique constraints y foreign keys necesarias.
5. Identificar tablas planeadas pero no usadas.

Tablas minimas a cubrir:

- `usuario`
- `campus`
- `carrera`
- `periodo`
- `periodo_evento`
- `curso`
- `silabo`
- `silabo_unidad`
- `silabo_tema`
- `silabo_evaluacion`
- `usuario_periodo`
- `usuario_periodo_curso`
- `usuario_periodo_curso_horario`
- `usuario_periodo_evaluacion`
- `usuario_preferencia_estudio`
- `usuario_periodo_curso_confianza`
- `calendar_sync_account`
- `whatsapp_link_codes`
- `user_whatsapp_links`
- `whatsapp_inbound_message`

Fase 2: decidir estrategia.

Opciones:

1. Crear `V1__base_schema.sql` desde cero.
   - Bueno si todavia no hay produccion importante.
   - Mas limpio para futuro.

2. Crear baseline Flyway sobre base existente.
   - Bueno si produccion ya tiene schema y datos.
   - Requiere cuidado para no romper datos.

Recomendacion:

- Si produccion ya tiene datos reales: usar baseline controlado.
- Si aun es MVP sin datos criticos: crear migracion base limpia.

Fase 3: crear migraciones.

Tareas:

1. Crear migracion base.
2. Mover o ajustar migracion WhatsApp para que corra despues del schema base.
3. Agregar indices importantes:
   - usuario por email.
   - curso por codigo.
   - usuario_periodo por usuario/periodo.
   - usuario_periodo_curso por usuario_periodo/curso.
   - evaluaciones por usuario_periodo_curso/codigo.
   - WhatsApp por user/status, code, wa_id, meta_message_id.

4. Definir seeds si aplica.
   - Campus/carreras/periodos/cursos/silabos pueden ser datos de catalogo.
   - Decidir si van en migraciones o carga separada.

Fase 4: prueba desde cero.

Tareas:

1. Crear base local vacia.
2. Ejecutar migraciones.
3. Levantar backend.
4. Probar endpoints de catalogo.
5. Probar onboarding.
6. Probar dashboard/me.
7. Probar WhatsApp link-code.

Criterios de aceptacion:

- Una base vacia puede quedar lista con migraciones versionadas.
- El backend arranca sin crear tablas automaticamente por Hibernate.
- No hay dependencia de SQL manual no documentado.
- La migracion WhatsApp queda ordenada respecto al schema base.
- Se documenta como inicializar DB local y produccion.

Comandos utiles:

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
mvn -DskipTests compile
```

```powershell
cd C:\Users\uu\Desktop\trackademy\trackademy-backend
mvn quarkus:dev
```

## QA manual posterior a Prioridad 1

Despues de completar P1, ejecutar este checklist:

1. Login Google.
2. Login Microsoft.
3. Usuario nuevo sin onboarding.
4. Completar onboarding manual.
5. Completar onboarding con preview PDF.
6. Entrar al dashboard.
7. Ver cursos.
8. Editar profesor y seccion.
9. Configurar horario.
10. Ver horario semanal.
11. Registrar nota.
12. Ver notas agrupadas.
13. Ver calendario.
14. Ver tareas derivadas.
15. Ver recordatorios.
16. Editar perfil.
17. Reconfigurar ciclo.
18. Generar codigo WhatsApp.
19. Vincular WhatsApp por webhook.
20. Probar comandos WhatsApp.
21. Desvincular WhatsApp.
22. Cerrar sesion.

## Prioridades posteriores

Prioridad 2:

- QA manual completo.
- Tests backend minimos.
- Tests frontend minimos.

Prioridad 3:

- Endurecer WhatsApp.
- Mejorar comandos solo despues de estabilizar seguridad.

Prioridad 4:

- Tareas manuales persistentes.
- Recordatorios persistentes.
- Sincronizacion real con Google/Outlook Calendar.

Prioridad 5:

- Limpiar warnings.
- Ordenar documentacion duplicada.
- Revisar encoding de README si molesta.
