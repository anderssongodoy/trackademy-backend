# Frontend Syllabus API Contract

Fecha: 2026-04-20

Este documento resume las APIs de backend que el frontend puede usar para mostrar:

- detalle del curso
- silabo vigente
- historial de silabos
- descarga de PDF del silabo
- navegacion estable del curso usando `publicId`

## Idea principal

El frontend no deberia depender solo de `codigo`.

Cada curso ahora expone:

- `id`
- `publicId`
- `codigo`
- `nombre`

La recomendacion es:

- usar `publicId` como identificador estable de navegacion
- seguir mostrando `codigo` como dato academico visible

## Endpoints disponibles

### 1. Listar cursos

```http
GET /api/v1/catalog/cursos
GET /api/v1/catalog/cursos?carreraId=2
GET /api/v1/catalog/cursos?carreraId=2&q=integrador
```

Respuesta por item:

```json
{
  "id": 16,
  "publicId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
  "codigo": "100000S09I",
  "nombre": "Curso integrador II: sistemas",
  "creditos": 4,
  "horasSemanales": 4,
  "modalidad": "Presencial",
  "cicloReferencial": 9
}
```

Uso frontend:

- listado de cursos
- buscador
- malla por ciclo

### 2. Obtener curso por codigo

```http
GET /api/v1/catalog/cursos/{codigo}
```

Ejemplo:

```http
GET /api/v1/catalog/cursos/100000S09I
```

Uso frontend:

- compatibilidad
- deep links antiguos basados en codigo

### 3. Obtener curso por `publicId`

```http
GET /api/v1/catalog/cursos/public/{publicId}
```

Uso frontend:

- ruta estable del curso
- recomendado para navegacion nueva

### 4. Obtener detalle del curso por codigo

```http
GET /api/v1/catalog/cursos/{codigo}/detalle
```

### 5. Obtener detalle del curso por `publicId`

```http
GET /api/v1/catalog/cursos/public/{publicId}/detalle
```

Respuesta:

```json
{
  "curso": {
    "id": 16,
    "publicId": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
    "codigo": "100000S09I",
    "nombre": "Curso integrador II: sistemas",
    "creditos": 4,
    "horasSemanales": 4,
    "modalidad": "Presencial",
    "cicloReferencial": 9
  },
  "silaboId": 152,
  "version": "2026_2026 - Ciclo 1 Marzo_100000S09I",
  "pdf": {
    "assetId": 33,
    "originalFilename": "CI2SS_silabo_2026_1.pdf",
    "sourceFilename": "CI2SS_silabo_2026_1.pdf",
    "mimeType": "application/pdf",
    "sizeBytes": 123456,
    "sha256": "abc123",
    "storageProvider": "filesystem",
    "disponibleDescarga": true
  },
  "pdfDownloadPath": "/api/v1/catalog/cursos/silabos/152/pdf",
  "anio": 2026,
  "periodoTexto": "2026 - Ciclo 1 Marzo",
  "sumilla": "...",
  "fundamentacion": "...",
  "metodologia": "...",
  "logroGeneral": "...",
  "unidades": [],
  "evaluaciones": []
}
```

Uso frontend:

- pantalla principal del curso
- boton de descarga del silabo vigente
- render de unidades y evaluaciones

### 6. Obtener silabo vigente por codigo

```http
GET /api/v1/catalog/cursos/{codigo}/silabo-vigente
```

### 7. Obtener silabo vigente por `publicId`

```http
GET /api/v1/catalog/cursos/public/{publicId}/silabo-vigente
```

Respuesta:

```json
{
  "silaboId": 152,
  "version": "2026_2026 - Ciclo 1 Marzo_100000S09I",
  "vigente": true,
  "anio": 2026,
  "periodoTexto": "2026 - Ciclo 1 Marzo",
  "extraidoEn": "2026-04-20T00:00:00Z",
  "pdf": {
    "assetId": 33,
    "originalFilename": "CI2SS_silabo_2026_1.pdf",
    "sourceFilename": "CI2SS_silabo_2026_1.pdf",
    "mimeType": "application/pdf",
    "sizeBytes": 123456,
    "sha256": "abc123",
    "storageProvider": "filesystem",
    "disponibleDescarga": true
  },
  "pdfDownloadPath": "/api/v1/catalog/cursos/silabos/152/pdf"
}
```

Uso frontend:

- cuando solo se necesita metadata del silabo vigente
- boton rapido de descarga

### 8. Listar historial de silabos por codigo

```http
GET /api/v1/catalog/cursos/{codigo}/silabos
```

### 9. Listar historial de silabos por `publicId`

```http
GET /api/v1/catalog/cursos/public/{publicId}/silabos
```

Respuesta por item:

```json
{
  "silaboId": 152,
  "version": "2026_2026 - Ciclo 1 Marzo_100000S09I",
  "vigente": true,
  "anio": 2026,
  "periodoTexto": "2026 - Ciclo 1 Marzo",
  "extraidoEn": "2026-04-20T00:00:00Z",
  "pdf": {
    "assetId": 33,
    "originalFilename": "CI2SS_silabo_2026_1.pdf",
    "sourceFilename": "CI2SS_silabo_2026_1.pdf",
    "mimeType": "application/pdf",
    "sizeBytes": 123456,
    "sha256": "abc123",
    "storageProvider": "filesystem",
    "disponibleDescarga": true
  },
  "pdfDownloadPath": "/api/v1/catalog/cursos/silabos/152/pdf"
}
```

Uso frontend:

- modal o seccion "Historial de silabos"
- tabla de versiones
- boton "Descargar" por version

### 10. Descargar PDF de un silabo

```http
GET /api/v1/catalog/cursos/silabos/{silaboId}/pdf
```

Ejemplo:

```http
GET /api/v1/catalog/cursos/silabos/152/pdf
```

Comportamiento:

- responde `application/pdf`
- si el archivo no existe o no esta disponible, responde `404`

## Reglas para frontend

### Navegacion

Recomendacion:

- usar `publicId` en las rutas internas del frontend
- mantener `codigo` solo como dato visible o compatibilidad

Ejemplo:

```text
/cursos/{publicId}
```

### Pantalla del curso

Recomendacion:

1. cargar `GET /api/v1/catalog/cursos/public/{publicId}/detalle`
2. renderizar:
   - nombre
   - codigo
   - ciclo
   - version del silabo
   - unidades
   - evaluaciones
3. si `pdfDownloadPath != null`, mostrar boton "Descargar silabo"
4. cargar historial cuando el usuario abra la seccion de versiones

### Historial de silabos

Mostrar:

- version
- periodo
- si es vigente
- fecha de extraccion
- boton descargar si `pdf.disponibleDescarga == true`

Si `pdf.disponibleDescarga == false`:

- mostrar "PDF no disponible"
- no mostrar boton roto

### Fallback si el frontend aun usa codigo

Si todavia no migran a `publicId`, pueden usar:

- `GET /api/v1/catalog/cursos/{codigo}/detalle`
- `GET /api/v1/catalog/cursos/{codigo}/silabos`
- `GET /api/v1/catalog/cursos/{codigo}/silabo-vigente`

Pero la recomendacion sigue siendo migrar a `publicId`.

## Casos borde que frontend debe contemplar

### 1. El curso cambio de codigo

- el backend debe poder seguir resolviendo el curso historicamente
- el frontend no debe asumir que `codigo` es estable para siempre

### 2. El curso cambio de nombre

- el historial sigue perteneciendo al mismo curso
- el frontend debe tratarlo como el mismo recurso si el `publicId` es el mismo

### 3. Hay historial, pero una version vieja no tiene PDF descargable

- mostrar la version
- mostrar estado "sin PDF disponible"
- no intentar descargar

### 4. El detalle del curso no tiene silabo vigente

Puede ocurrir en cursos sin ingestion completa.

El frontend debe tolerar:

- `silaboId = null`
- `version = null`
- `pdf = null`
- `pdfDownloadPath = null`

## Recomendacion de implementacion en frontend

### Card o vista del curso

Usar:

```http
GET /api/v1/catalog/cursos/public/{publicId}/detalle
```

### Boton "Descargar silabo actual"

Usar:

- `pdfDownloadPath` del detalle

### Modal "Ver historial"

Usar:

```http
GET /api/v1/catalog/cursos/public/{publicId}/silabos
```

### Boton "Descargar version historica"

Usar:

- `pdfDownloadPath` del item de historial

## Estado actual

Estas APIs ya quedaron listas a nivel de backend de aplicacion.

Puntos a recordar:

- la descarga real solo funciona si el silabo tiene `pdf_asset_id` y el archivo existe en filesystem
- algunas versiones historicas antiguas pueden seguir sin asset descargable hasta hacer backfill de archivos
