# StudyCards - Backend

API REST de **StudyCards**, una aplicación de tarjetas de estudio (flashcards) para Android.
Proyecto de la materia Arquitectura de Software 2, Universidad Central.

- **Stack:** Quarkus 3 (Java 21), Maven, Hibernate ORM con Panache, PostgreSQL.
- **Frontend (Flutter):** repositorio `studycards-frontend`.

## Requisitos

- Java 21
- Una base de datos PostgreSQL (local o en la nube)

## Configuración de la base de datos

Los datos de conexión **no están en el código**: se leen de variables de entorno.

1. Copia `.env.example` como `.env` en la raíz del proyecto (ya está en el `.gitignore`).
2. Completa `DB_URL`, `DB_USER` y `DB_PASSWORD`.
    - **Nube (Render Postgres):** usa el host largo de la *External Database URL* y `?sslmode=require`.
    - **Local:** si solo defines `DB_PASSWORD`, se conecta a `localhost:5432`, base `studycards`, usuario `postgres`.

Las tablas se crean y actualizan solas al arrancar (Hibernate `database-generation=update`).

## Ejecutar en desarrollo

```
./mvnw quarkus:dev        (en Windows: .\mvnw quarkus:dev)
```

La API queda en `http://localhost:8080`. Quarkus lee el archivo `.env` automáticamente en modo desarrollo.

## Endpoints

### Materias

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/materias` | Lista las materias (orden alfabético) con total de tarjetas y pendientes |
| POST | `/materias` | Crea una materia (`nombre` obligatorio, `descripcion` y `color` opcionales) |
| PUT | `/materias/{id}` | Edita nombre, descripción y color |
| DELETE | `/materias/{id}?confirmar=false` | Elimina una materia. Si tiene tarjetas y no se confirma, responde **409** con el total; con `confirmar=true` las elimina también |
| GET | `/materias/{id}/pendientes` | Total de tarjetas y cuántas están pendientes de repaso |

### Tarjetas

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/materias/{id}/tarjetas` | Lista las tarjetas de la materia |
| POST | `/materias/{id}/tarjetas` | Crea una tarjeta (`pregunta` y `respuesta` obligatorias) |
| PUT | `/materias/{id}/tarjetas/{tarjetaId}` | Edita pregunta y respuesta |
| DELETE | `/materias/{id}/tarjetas/{tarjetaId}` | Elimina una tarjeta |
| PUT | `/materias/{id}/tarjetas/{tarjetaId}/nivel` | Marca la tarjeta como `APRENDIDA` o `REPASAR` |

### Sesión de estudio

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/materias/{id}/estudio` | Tarjetas a estudiar en orden aleatorio. Si hay tarjetas **vencidas** devuelve solo esas (`modo: VENCIDAS`); si no, toda la materia (`modo: ALEATORIO`) |

**Regla de tarjetas vencidas:** una tarjeta nueva tiene 5 días para ser aprendida
(`fechaProximoRepaso` = creación + 5 días). Está *vencida* si sigue en `REPASAR` y esa
fecha es hoy o anterior (las que no tienen fecha también cuentan). Al marcar una tarjeta como
`REPASAR`, queda vencida desde hoy.

## Datos de demostración

Con el backend encendido, el script `demo/cargar_datos_demo.py` carga 3 materias con tarjetas
de ejemplo a través de la API (omite las que ya existen):

```
py demo/cargar_datos_demo.py
py demo/cargar_datos_demo.py https://<url-del-backend>
```

## Flujo de trabajo (Git)

- `main`: versión estable.
- `develop`: integración.
- Una rama por historia de usuario, creada desde `develop` y con PR hacia `develop`:
  `HU0X-nombre-backend`.

## Despliegue (Render)

El `Dockerfile` compila el proyecto con Maven y ejecuta la aplicación con una imagen ligera de Java.
En el servicio de Render se definen las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`
(la variable `PORT` la asigna Render).