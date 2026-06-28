# openHouse (testing)

> Plataforma de alquiler vacacional estilo Airbnb construida con **Java y Spring Boot** (listados,
> reservas, reseñas, amenities y mensajería entre huéspedes y anfitriones), centrada en la
> **automatización de tests**: tests unitarios y de slice, MockMvc, Selenium E2E y suite completa
> con cobertura vía JaCoCo y análisis estático con SonarCloud.

<p align="center">
  <img src="https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 4.0.5">
  <img src="https://img.shields.io/badge/Tests-JUnit%205-25A162?logo=junit5&logoColor=white" alt="JUnit 5">
  <img src="https://img.shields.io/badge/E2E-Selenium-43B02A?logo=selenium&logoColor=white" alt="Selenium">
  <img src="https://img.shields.io/badge/Cobertura-JaCoCo-F01F7A" alt="JaCoCo">
  <img src="https://img.shields.io/badge/Calidad-SonarCloud-F3702A?logo=sonarcloud&logoColor=white" alt="SonarCloud">
  <img src="https://img.shields.io/badge/DB-H2-1F6FEB" alt="H2">
</p>

---

## ¿Qué es?

Una aplicación web completa renderizada en servidor (MVC con Thymeleaf, no una API JSON), usada
como proyecto final del grupo de testing para practicar pruebas automatizadas en los tres niveles
de la pirámide:

- **Cualquier visitante** explora el catálogo de alojamientos, filtra por tipo, precio y capacidad,
  y consulta reseñas.
- **Usuarios registrados** realizan reservas, escriben reseñas y se comunican con el anfitrión
  mediante mensajería integrada.
- **Anfitriones (ROLE_HOST)** publican y gestionan sus propios listados.
- **Administradores (ROLE_ADMIN)** gestionan listados, amenities, reservas y usuarios desde el
  panel de administración.

Sobre esa base, el proyecto cuenta con una **suite de 275 tests** que cubre repositorios,
controladores, servicios, seguridad y UI con Selenium E2E.

---

## Funcionalidades principales

### Catálogo y descubrimiento
- Listado de alojamientos con **filtros combinables** por tipo de alojamiento, precio mínimo /
  máximo, número de huéspedes y noches.
- Ordenación por precio (ascendente / descendente), fecha de publicación y valoración media.
- Ficha de cada alojamiento con descripción, amenities, precio por noche y reseñas.
- Solo se muestran alojamientos **activos** (borrado lógico con campo `isActive`).

### Reservas
- Reserva con fechas de entrada / salida, número de huéspedes y cálculo automático del precio
  total.
- Estados de reserva: `PENDING` → `CONFIRMED` → `CANCELED`.
- El anfitrión confirma o cancela desde el panel de reservas.

### Reseñas
- Reseñas con **puntuación de 1 a 5** y comentario, asociadas a una reserva completada.
- Valoración media por listing calculada con proyección JPQL (`ListingRatingProjection`).

### Mensajería
- Conversación vinculada a cada reserva: huésped y anfitrión intercambian mensajes en tiempo real.
- Edición y borrado de mensajes propios, con control de acceso por usuario.

### Amenities
- Catálogo de amenities por tipo (`AmenityType`): cada listing puede tener múltiples amenities
  mediante la entidad intermedia `AmenityLine`.

### Cuentas, seguridad y administración
- **Registro** con validación de contraseña segura: mínimo 8 caracteres, al menos una mayúscula,
  una minúscula, un número y un carácter especial.
- **Inicio de sesión** con Spring Security (BCrypt) y control de acceso por roles.
- Panel de administración (`/dashboard`) exclusivo para `ROLE_ADMIN`.

---

## Modelo de datos

| Entidad | Descripción |
|---|---|
| **User** | Usuario con rol (`ROLE_USER` / `ROLE_HOST` / `ROLE_ADMIN`), implementa autenticación con Spring Security. |
| **Listing** | Alojamiento: título, descripción corta/larga, precio por noche, mínimo/máximo de noches, capacidad, tipo (`ListingType`), ciudad (`City`), estado activo y fecha de publicación. |
| **Booking** | Reserva: fechas, precio total, estado (`PENDING` / `CONFIRMED` / `CANCELED`), huésped y listing. |
| **Review** | Reseña con puntuación 1-5, comentario, verificación y fecha, asociada a una reserva. |
| **Amenity** | Amenity con nombre, descripción, icono y tipo (`AmenityType`), vinculada a un listing. |
| **AmenityLine** | Entidad intermedia que relaciona un listing con sus amenities. |
| **Conversation** | Conversación vinculada 1:1 a una reserva. |
| **Message** | Mensaje dentro de una conversación: contenido, fecha de envío, estado de lectura y remitente. |

> Nota: `ListingType` cubre APARTMENT, HOUSE, LOFT, VILLA, STUDIO y CABIN.
> `City` incluye MADRID, BARCELONA, VALENCIA, ALICANTE, SEVILLA y BILBAO.

---

## Stack tecnológico

- **Java 25** + **Spring Boot 4.0.5** · **Apache Maven 3.9.14**
- **Spring MVC** + **Thymeleaf**, **Spring Data JPA / Hibernate**, **Spring Security**
- Base de datos: **H2** en memoria (desarrollo y tests)
- **Bootstrap 5.3.8** + **Font Awesome 7.2.0** (vía WebJars) · **SpringDoc OpenAPI 3.0.2**
- **Testing**:
    - **JUnit 5** como motor de tests.
    - **Mockito** para tests unitarios de servicios.
    - **MockMvc** para tests de controladores.
    - **Selenium 4.44.0** para tests E2E de la interfaz.
    - **JaCoCo 0.8.13** para cobertura de código.
    - **SonarCloud** para análisis estático (organización: `certidevs`).
- **CI/CD**: GitHub Actions (`ubuntu-latest`, JDK Temurin 25).

---

## Ejecutar los tests

No es necesario ninguna configuración previa. H2 se configura automáticamente en el perfil `test`.

```bash
mvnw.cmd test                              # toda la suite (Windows)
./mvnw test                                # toda la suite (Linux / macOS)

mvnw.cmd test -Dtest="*RepositoryTest"     # solo repositorios (@DataJpaTest, H2)
mvnw.cmd test -Dtest="*ControllerTest"     # solo controladores (@SpringBootTest + MockMvc, H2)
mvnw.cmd test -Dtest="*ServiceTest"        # solo servicios (Mockito, sin contexto Spring)
mvnw.cmd test -Dtest="*SecurityTest"       # solo seguridad (rutas de SecurityConfig)
mvnw.cmd test -Dtest="*SeleniumTest"       # solo Selenium E2E (headless automático en CI)
```

En IntelliJ: clic derecho sobre `src/test/java` → **Run Tests**.

Pirámide de tests del proyecto (275 tests en total):

| Capa | Herramienta | Base de datos | Nº tests |
|---|---|---|:---:|
| Repositorios | `@DataJpaTest` | H2 | ~40 |
| Controladores | `@SpringBootTest` + MockMvc | H2 | ~60 |
| Servicios | `@ExtendWith(MockitoExtension)` | — (mocks) | ~10 |
| Seguridad | MockMvc + Spring Security Test | H2 | ~4 |
| E2E | Selenium (headless) | H2 | ~60 |
| **Total** | | | **275** |

> Los tests de Selenium se ejecutan en modo headless automáticamente cuando la variable de entorno
> `CI=true` está activa (GitHub Actions la establece por defecto).

---

## Suites de tests

El proyecto incluye suites para ejecutar grupos de tests de una sola vez:

| Suite | Clase | Incluye |
|---|---|---|
| Todos los tests | `AllTestsSuite` | Toda la suite |
| Solo controladores | `ControllerTestSuite` | `*ControllerTest` |
| Solo repositorios | `RepositoryTestsSuite` | `*RepositoryTest` |
| Solo Selenium | `SeleniumTestsSuite` | `*SeleniumTest` |

---

## Integración continua (GitHub Actions)

Workflows en `.github/workflows` (manuales — `workflow_dispatch`):

- **`tests.yml`**: ejecuta tests por categoría en H2 en jobs paralelos (`*RepositoryTest`,
  `*ControllerTest`, `*ServiceTest`, `*SecurityTest`, `*SeleniumTest`). Chrome preinstalado en
  `ubuntu-latest` para los tests de Selenium. Guarda capturas de fallos como artefacto.
- **`sonar.yml`**: `mvn verify` — suite completa + JaCoCo + análisis SonarCloud. Sube cobertura
  e informe de calidad automáticamente.

---

## Arrancar la aplicación

> Requisitos: **JDK 25**. El wrapper de Maven (`mvnw.cmd` en Windows) está incluido en el repo.

```bash
mvnw.cmd spring-boot:run      # Windows
./mvnw spring-boot:run        # Linux / macOS
```

App disponible en **http://localhost:8080** · Consola H2 en **http://localhost:8080/h2-console**
(JDBC URL `jdbc:h2:mem:openhouse_db`, usuario `sa`, contraseña vacía).

---

## Cuentas de demo

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin@openhouse.com` | `1234` | Administrador |
| `alex@pro.com` | `1234` | Anfitrión (HOST) |
| `juan@pro.com` | `1234` | Anfitrión (HOST) |
| `sonia@mail.com` | `1234` | Usuario estándar |
| `maria@pro.com` | `1234` | Usuario estándar |
| `pedro@mail.com` | `1234` | Usuario estándar |

---

## Estructura del proyecto

```
src/main/java/com/demo
├── config/          # SecurityConfig, WebConfig, DataInitializer (@Profile("!test"))
├── controllers/     # AuthController, ListingController, BookingController,
│                    # ReviewController, AmenityController, ConversationController,
│                    # UserController, AdminController
├── dto/             # RegisterDTO
├── model/           # Entidades JPA: User, Listing, Booking, Review,
│                    # Amenity, AmenityLine, Conversation, Message
│   └── enums/       # Role, ListingType, BookingStatus, AmenityType, City
├── repositories/    # Repositorios Spring Data JPA + ListingRatingProjection
└── services/        # UserService, ListingService, FileService

src/test/java/com/demo
├── AllTestsSuite.java
├── ControllerTestSuite.java
├── RepositoryTestsSuite.java
├── SeleniumTestsSuite.java
├── controllers/     # *ControllerTest (MockMvc + @SpringBootTest)
├── repositories/    # *RepositoryTest (@DataJpaTest)
├── security/        # SecurityTest
├── services/        # *ServiceTest (Mockito)
└── ui/              # *SeleniumTest (Selenium E2E headless)
    └── BaseSeleniumTest.java + ScreenshotOnFailure.java

src/main/resources
├── application.properties          # H2 en memoria (perfil por defecto)
└── templates/                      # Vistas Thymeleaf por entidad
src/test/resources
└── application.properties          # H2 para tests (no modificar)
```

---

<p align="center">
  Hecho con Java y Spring Boot · Grupo 3 · <a href="https://github.com/certidevs/g3_testing">certidevs/g3_testing</a>
</p>