# WhatsApp API Contract

## Objetivo

Este documento define el contrato de APIs backend para la futura integracion del frontend con la vinculacion de WhatsApp en Trackademy.

El frontend no autentica por WhatsApp.

El flujo correcto es:

- el alumno entra con Google o Microsoft
- la web autenticada solicita un codigo temporal
- la web abre WhatsApp con ese codigo precargado
- el alumno envia el primer mensaje
- el backend valida, vincula y responde

## Endpoint 1

### `POST /api/v1/whatsapp/link-code`

Endpoint autenticado.

Genera un codigo temporal nuevo para el usuario autenticado e invalida cualquier codigo activo anterior.

#### Response 200

```json
{
  "code": "TDK-482913",
  "expiresAt": "2026-04-09T18:25:00Z",
  "officialWhatsappNumber": "51999999999",
  "instructions": "Abre WhatsApp y envia este codigo al numero oficial de Trackademy.",
  "prefilledMessage": "TDK-482913",
  "deepLink": "https://wa.me/51999999999?text=TDK-482913"
}
```

#### Reglas

- requiere `Authorization: Bearer ...`
- si ya existe un codigo activo, se cancela antes de emitir otro
- TTL recomendado: 10 minutos

## Endpoint 2

### `GET /api/v1/whatsapp/link-status`

Endpoint autenticado.

Devuelve el estado actual de vinculacion del usuario autenticado.

#### Response 200

```json
{
  "linked": true,
  "phoneNumberMasked": "+51 *** *** 154",
  "linkedAt": "2026-04-09T18:17:00Z",
  "lastInteractionAt": "2026-04-09T18:19:42Z"
}
```

#### Caso sin vinculo

```json
{
  "linked": false,
  "phoneNumberMasked": null,
  "linkedAt": null,
  "lastInteractionAt": null
}
```

## Endpoint 3

### `DELETE /api/v1/whatsapp/link`

Endpoint autenticado.

Desvincula el numero de WhatsApp del usuario autenticado.

#### Response 204

Sin body.

#### Regla

- es opcional para fase 1, pero recomendable si entra limpio

## Endpoint 4

### `GET /api/v1/webhooks/whatsapp`

Endpoint publico para verificacion de Meta.

#### Query esperada por Meta

- `hub.mode`
- `hub.verify_token`
- `hub.challenge`

#### Comportamiento

- si el token coincide, responder el `challenge`
- si no coincide, responder `403`

#### Response 200 ejemplo

```text
1158201444
```

## Endpoint 5

### `POST /api/v1/webhooks/whatsapp`

Endpoint publico para eventos entrantes de WhatsApp Cloud API.

#### Comportamiento esperado

- leer payload recibido desde Meta
- detectar mensajes de texto entrantes
- extraer `wa_id`, telefono y texto
- si el texto coincide con un codigo pendiente:
  - validar expiracion y estado
  - vincular el numero al usuario correcto
  - responder confirmacion
- si el remitente ya esta vinculado:
  - resolver comando
  - consultar datos reales
  - responder por WhatsApp
- si no esta vinculado:
  - responder instruccion para vincular desde la web

#### Response recomendado

- `200 OK` rapido, aunque el procesamiento interno haga mas trabajo despues

## Comandos MVP soportados

### `menu`

```text
Comandos disponibles: resumen, pendientes, hoy, cursos, ayuda.
```

### `ayuda`

```text
Este canal te permite consultar informacion ya registrada en Trackademy. Por ahora no permite registrar ni editar datos.
```

### `resumen`

Debe usar data real del dashboard del alumno.

Contenido sugerido:

- cantidad de cursos activos
- cantidad de evaluaciones pendientes
- proximas evaluaciones
- proxima clase o evento si existe

### `pendientes`

Debe listar evaluaciones del alumno sin nota y con prioridad temporal.

### `hoy`

Debe listar eventos o clases del dia y pendientes relevantes del dia si existen.

### `cursos`

Debe listar cursos activos del periodo actual del alumno.

## Respuestas controladas

### Codigo valido

```text
Tu cuenta de Trackademy fue vinculada correctamente. Ya puedes escribir: menu, resumen, pendientes, hoy o cursos.
```

### Codigo expirado

```text
Este codigo de vinculacion ya expiro. Genera uno nuevo desde tu cuenta de Trackademy.
```

### Codigo invalido

```text
No reconoci ese codigo. Genera uno nuevo desde tu cuenta de Trackademy e intentalo otra vez.
```

### Usuario no vinculado

```text
Tu numero aun no esta vinculado a una cuenta de Trackademy. Ingresa a la web, genera tu codigo de vinculacion y envialo por este chat.
```

### Comando no soportado

```text
Por ahora este canal es solo de consulta. Escribe: menu, resumen, pendientes, hoy o cursos.
```

### Intento de escritura

```text
Por ahora WhatsApp en Trackademy es solo para consultar informacion. El registro y edicion de datos se realiza desde la web.
```

## Contrato esperado para frontend

El frontend solo necesita, en fase 1:

- generar codigo
- leer estado de vinculacion
- opcionalmente desvincular
- mostrar numero oficial
- abrir el `deepLink`
- hacer polling de `link-status` mientras el codigo siga activo

## Recomendacion de experiencia para frontend

Pantalla sugerida:

- `Perfil`

Seccion sugerida:

- `Integraciones y canales`

Acciones:

- `Conectar WhatsApp`
- `Abrir WhatsApp`
- `Copiar codigo`
- `Generar nuevo codigo`
- `Desvincular` si esa API entra en fase 1

## Variables de entorno requeridas

- `APP_WHATSAPP_META_ACCESS_TOKEN`
- `APP_WHATSAPP_META_PHONE_NUMBER_ID`
- `APP_WHATSAPP_META_WEBHOOK_VERIFY_TOKEN`
- `APP_WHATSAPP_META_API_VERSION`
- `APP_WHATSAPP_OFFICIAL_NUMBER`
- `APP_WHATSAPP_LINK_CODE_TTL_SECONDS`

## Notas operativas

- el frontend debe empujar siempre a que el alumno envie el primer mensaje
- no se debe presentar WhatsApp como mecanismo de login
- si se rota el numero oficial o el `phone-number-id`, el frontend no debe hardcodearlo
- el `deepLink` debe venir listo desde backend para evitar inconsistencias
