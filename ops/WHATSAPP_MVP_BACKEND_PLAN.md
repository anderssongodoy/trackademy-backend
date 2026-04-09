# WhatsApp MVP Backend Plan

## Objetivo

Implementar en el backend de Trackademy una integracion MVP con WhatsApp Cloud API de Meta para consultas rapidas por chat, sin cambiar el modelo actual de autenticacion del producto.

Principio base:

- el login del alumno sigue siendo solo con Google o Microsoft
- WhatsApp no autentica usuarios
- WhatsApp se vincula a una cuenta ya autenticada en la web
- el canal de WhatsApp en esta fase es solo de consulta

## Estado actual del backend

Hoy el backend ya tiene una base funcional que encaja bien con esta integracion:

- autenticacion web con Google y Microsoft
- JWT propio para sesion del backend
- modelo de usuario simple basado en `usuario`
- periodo actual del alumno
- dashboard resumido
- cursos, horarios, evaluaciones y calendario

Piezas principales ya existentes:

- `adapter/in/rest/AuthResource`
- `application/service/AuthService`
- `adapter/out/auth/JwtTokenAdapter`
- `adapter/in/rest/MeResource`
- `application/service/MeQueryService`
- `adapter/out/persistence/PostgresMeQueryAdapter`

## Decision de arquitectura

La integracion de WhatsApp debe entrar como una subcapacidad nueva, separada de auth web y reutilizando la capa `me` como fuente real de datos.

Capas recomendadas:

- `adapter/in/rest`: endpoints internos de vinculacion y webhook de Meta
- `application/port/in`: casos de uso de vinculacion, webhook y comandos
- `application/service`: orquestacion
- `application/port/out`: contrato para enviar mensajes a Meta
- `adapter/out/whatsapp`: cliente Cloud API
- `adapter/out/persistence`: entidades, repositorios y adaptadores de persistencia
- `domain/model/whatsapp`: modelos simples si hace falta formalizar estados y resultados

## Flujo funcional aprobado

### 1. Vinculacion

1. El alumno inicia sesion en Trackademy con Google o Microsoft.
2. Desde la web autenticada solicita vincular WhatsApp.
3. El backend genera un codigo temporal amigable, por ejemplo `TDK-482913`.
4. El backend invalida cualquier codigo activo anterior del mismo usuario.
5. La web muestra el codigo y abre WhatsApp con el texto precargado.
6. El alumno envia ese mensaje al numero oficial de Trackademy.
7. El webhook recibe el mensaje entrante.
8. Si el codigo existe, no expiro y no fue usado:
   - se vincula `wa_id` con `user_id`
   - se marca el codigo como `USED`
   - se registra el vinculo como verificado
   - se responde por WhatsApp confirmando la vinculacion

### 2. Consulta

Una vez vinculado el numero:

- `menu`
- `ayuda`
- `resumen`
- `pendientes`
- `hoy`
- `cursos`

No se debe permitir:

- registrar notas
- registrar tareas
- editar datos
- flujos abiertos de chat

## Fuente real de datos para comandos

La integracion no debe inventar repositorios paralelos si ya existe logica reutilizable.

Mapeo recomendado:

- `menu`: texto estatico
- `ayuda`: texto estatico
- `cursos`: `listarMisCursos(email)`
- `resumen`: `obtenerDashboard(email)`
- `hoy`: `listarCalendario(email, hoy, hoy)`
- `pendientes`: evaluaciones del alumno sin nota, derivadas desde la capa `me`

La fuente principal de datos es:

- `adapter/out/persistence/PostgresMeQueryAdapter`

## Endpoints backend previstos

### Internos autenticados

- `POST /api/v1/whatsapp/link-code`
- `GET /api/v1/whatsapp/link-status`
- `DELETE /api/v1/whatsapp/link`

### Webhook Meta

- `GET /api/v1/webhooks/whatsapp`
- `POST /api/v1/webhooks/whatsapp`

## Persistencia propuesta

### Tabla 1: `whatsapp_link_codes`

- `id`
- `user_id`
- `code`
- `status`
- `expires_at`
- `used_at`
- `created_at`

Estados:

- `PENDING`
- `USED`
- `EXPIRED`
- `CANCELLED`

### Tabla 2: `user_whatsapp_links`

- `id`
- `user_id`
- `wa_id`
- `phone_number`
- `verified`
- `linked_at`
- `last_interaction_at`

Reglas:

- un usuario solo puede tener un vinculo activo
- un `wa_id` solo puede pertenecer a un usuario
- un codigo solo puede usarse una vez

## Variables de configuracion requeridas

En `application.properties` se agregarian variables como:

- `app.whatsapp.meta.access-token`
- `app.whatsapp.meta.phone-number-id`
- `app.whatsapp.meta.webhook-verify-token`
- `app.whatsapp.meta.api-version`
- `app.whatsapp.official-number`
- `app.whatsapp.link-code.ttl-seconds`

## Reglas de seguridad

- no exponer datos personales si el numero no esta vinculado
- no loggear secretos ni tokens
- no aceptar comandos de escritura
- actualizar `lastInteractionAt` solo para remitentes ya vinculados
- rechazar intentos de reasignar un `wa_id` ya vinculado a otro usuario
- dejar preparada una capa para validar firma del webhook en una fase siguiente

## Alcance de fase 1

### Si entra en fase 1

- generacion y consumo de codigo temporal
- webhook GET/POST
- persistencia del vinculo
- envio de mensajes de texto por Meta
- comandos de consulta basicos
- respuestas controladas para mensajes no soportados

### Queda fuera o como placeholder limpio

- validacion completa de firma del webhook
- outbox, colas y reintentos sofisticados
- historial de conversacion
- templates avanzados
- mensajes masivos
- recordatorios automaticos
- comandos de escritura
- NLP o IA conversacional

## Observaciones de Meta y operacion

- la conversacion debe iniciarla el alumno desde WhatsApp
- el frontend debe abrir WhatsApp con el codigo precargado para que el alumno mande el primer mensaje
- no conviene iniciar outreach desde Trackademy en este MVP
- el numero y el token de Meta deben mantenerse fuera del codigo
- cualquier token expuesto en capturas debe rotarse antes de pruebas reales

## Entregable esperado tras implementacion

Al terminar la implementacion backend deberiamos tener:

- endpoints listos para vinculacion
- webhook verificable desde Meta
- mensajes salientes de texto simple por Cloud API
- comandos de consulta funcionando sobre data real
- documento de API listo para el equipo frontend
