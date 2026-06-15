# Proyecto: Alquiler Vacacional

Imagina que te encargan construir una plataforma de alquiler vacacional tipo Airbnb (pero mucho más sencilla): los anfitriones publican sus casaes, los huéspedes las reservan para unas fechas y después dejan una valoración. Es lo que vamos a construir entre todo el grupo.

La aplicación tiene **4 entidades principales**. Cada persona del grupo se encarga de una. A continuación se explica qué representa cada una, por qué la necesitamos y qué información guarda.


## La entidad User

`User` ya existe en el proyecto base (no hay que crearla). En esta aplicación un User puede tener dos papeles:

- Como **anfitrión**: publica casaes en la plataforma para que otros las reserven. Un anfitrión puede tener varias casaes.
- Como **huésped**: busca casaes y hace reservas para sus vacaciones.
- Un mismo User puede ser anfitrión de unas casaes y huésped en otras.
- Un **administrador** (rol ADMIN) modera la plataforma: puede revisar casaes y eliminar reseñas inapropiadas.

Cuando una de vuestras entidades necesite saber "quién es el dueño" o "quién hizo la reserva", se vincula con `User` mediante una asociación `@ManyToOne`. No tenéis que programar nada en `User`: solo la usáis como referencia.

---

## Fase 1 — Entidades

Cada alumno crea **una sola entidad** en `model/`. Como cada alumno toca archivos distintos, no hay conflictos de Git.

### House

Una **casa** es un alojamiento que un anfitrión publica en la plataforma para alquilar. Es la entidad central de la aplicación: sin casaes no hay nada que reservar. Piensa en lo que ves al buscar en Airbnb: cada tarjeta es una House.

Ejemplos reales: "Ático en Gran Vía — Madrid, 120 €/noche, hasta 4 huéspedes", "Casa rural con jardín — Segovia, 85 €/noche, hasta 6 huéspedes".

```
- id: Long               → identificador único, lo genera la base de datos
- title: String           → título del anuncio ("Ático en Gran Vía")
- description: String     → descripción del alojamiento ("Vistas al centro, reformado en 2024")
- pricePerNight: Double   → precio por noche en euros (120.0)
- location: String        → ciudad o zona ("Madrid")
- maxGuests: Integer      → número máximo de huéspedes permitidos (4)
```

### Booking

Una **reserva** es el registro de que un huésped ha reservado una casa para unas fechas concretas. Sin reservas, la plataforma sería solo un catálogo de fotos. Es lo que se genera cuando un huésped pulsa "Reservar" y elige sus fechas de entrada y salida.

Ejemplos reales: "María reserva el ático en Gran Vía del 1 al 5 de agosto, total 480 €, estado: confirmada".

```
- id: Long               → identificador único
- checkIn: LocalDate      → fecha de entrada (2026-08-01)
- checkOut: LocalDate     → fecha de salida (2026-08-05)
- totalPrice: Double      → precio total de la estancia (480.0, calculado como noches × precio/noche)
- status: String          → estado de la reserva ("pending", "confirmed", "cancelled", "completed")
```

### Review

Una **valoración** es la opinión que un huésped deja sobre una casa después de su estancia. Las reseñas son fundamentales para que otros huéspedes sepan si una casa merece la pena y para que los anfitriones mantengan la calidad. Piensa en las estrellas y comentarios que ves en Airbnb debajo de cada casa.

Ejemplos reales: "★★★★★ — Ubicación inmejorable, el piso estaba impecable y el anfitrión muy atento".

```
- id: Long               → identificador único
- rating: Integer         → puntuación de 1 a 5 (5)
- comment: String         → texto de la opinión ("Ubicación inmejorable...")
- createdAt: LocalDateTime → cuándo se escribió la reseña (2026-08-06T10:00)
```

### Amenity

Un **equipamiento** (o servicio) es algo que ofrece una casa para hacerla más atractiva. Los huéspedes filtran por amenities cuando buscan alojamiento ("quiero uno con piscina y wifi"). Tener un catálogo de amenities permite reutilizarlas entre casaes.

Ejemplos reales: "WiFi", "Piscina", "Parking gratuito", "Aire acondicionado", "Cocina equipada", "Terraza", "Admite mascotas".

```
- id: Long               → identificador único
- name: String            → nombre del equipamiento ("WiFi")
- description: String     → descripción más detallada ("WiFi de fibra 600 Mbps en toda la vivienda")
- icon: String            → nombre del icono para mostrar en la web ("wifi", "pool", "parking")
```

---

## Fase 2 — Repositorios y datos

Una vez creada la entidad, cada alumno crea dos cosas más:

**El repositorio** (`repository/`) permite guardar y consultar datos en la base de datos sin escribir SQL. Spring Data JPA genera las operaciones automáticamente.

```java
public interface HouseRepository extends JpaRepository<House, Long> {
}
```

**El DataInitializer** (`config/`) carga datos de ejemplo automáticamente al arrancar la aplicación para que la base de datos no esté vacía. Así al abrir h2-console ya se ven filas con datos reales.

```java
@Component
@Profile("!test")
public class HouseDataInitializer implements CommandLineRunner {
    private final HouseRepository repository;

    public HouseDataInitializer(HouseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;
        repository.save(new House("Ático en Gran Vía", "Vistas al centro", 120.0, "Madrid", 4));
        repository.save(new House("Casa rural en Sierra", "Jardín y barbacoa", 85.0, "Segovia", 6));
        repository.save(new House("Estudio en la playa", "A 50m del mar", 70.0, "Valencia", 2));
    }
}
```

- `@Profile("!test")` hace que estos datos **no se carguen cuando se ejecutan los tests**, para no interferir con las aserciones.
- `if (repository.count() > 0) return;` evita duplicar datos si reinicias la aplicación.

Verificar en h2-console (`localhost:8080/h2-console`) que cada tabla tiene datos: `SELECT * FROM houses;`

---

## Fase 3 — Asociaciones @ManyToOne

Hasta ahora cada entidad vive aislada. En la realidad están conectadas: una casa la publica un anfitrión, una reserva es de una casa concreta hecha por un huésped concreto, y una reseña la escribe un huésped sobre una casa donde se alojó. Estas conexiones se representan con `@ManyToOne`.

- **House → User**: cada casa la publica **un** anfitrión (pero un anfitrión puede tener muchas casaes)
- **Booking → House**: cada reserva es de **una** casa (pero una casa puede tener muchas reservas a lo largo del año)
- **Booking → User**: cada reserva la hace **un** huésped (pero un huésped puede hacer muchas reservas)
- **Review → House**: cada reseña es sobre **una** casa (pero una casa puede tener muchas reseñas)
- **Review → User**: cada reseña la escribe **un** huésped (pero un huésped puede escribir muchas reseñas)

```
User (anfitrión) ←── House ←── Review ──→ User (huésped)
                        ↑                      ↑
                        │                      │
                      Booking ─────────────────┘
```

> **Nota sobre Amenity**: la relación natural entre Amenity y House es muchos-a-muchos (una casa tiene muchos equipamientos, y un equipamiento como "WiFi" puede estar en muchas casaes). Este tipo de relación (`@ManyToMany`) es más avanzado, así que por ahora Amenity queda como un catálogo independiente. Más adelante se puede crear una tabla intermedia `HouseAmenity` para conectarlas.

Después de añadir las asociaciones, hay que actualizar los DataInitializers para que creen datos relacionados (por ejemplo, crear primero los Users, luego las Properties que apunten a ellos como anfitriones).

---

## Fase 4 — Controladores y HTML

Cada alumno crea un **controlador** y una **vista Thymeleaf** para que su entidad se pueda ver en el navegador, no solo en h2-console.

- `GET /houses` → listado de casaes mostrando título, ubicación, precio por noche y anfitrión
- `GET /bookings` → listado de reservas mostrando casa, fechas de check-in/check-out, precio total y huésped
- `GET /reviews` → reseñas mostrando casa, puntuación (estrellas), comentario y huésped
- `GET /amenities` → catálogo de equipamientos disponibles

Más adelante: formularios de creación, detalle (`GET /houses/{id}`), edición y borrado.

---

## Fase 5 — Ampliar el modelo

Cuando todo lo anterior funcione, se puede ampliar el proyecto con más campos, más entidades y más relaciones:

### Campos adicionales
- `House`: houseType (String: "apartamento", "casa", "habitación"), active (Boolean), imageUrl
- `Booking`: guests (Integer, cuántos huéspedes van), notes (String, peticiones especiales)
- `Review`: title (String, título corto de la reseña)
- `Amenity`: category (String: "básico", "confort", "exterior")

### Entidades nuevas
- **Photo**: fotos de una casa (un alojamiento puede tener muchas fotos). Campos: url, caption, uploadedAt.
- **Message**: mensajes entre anfitrión y huésped antes o durante una reserva. Campos: content, sentAt.
- **Payout**: pagos realizados al anfitrión por sus reservas completadas. Campos: amount, paidAt, status.

### Asociaciones nuevas
- `Photo → House` (`@ManyToOne`): cada foto pertenece a una casa
- `Message → User` (`@ManyToOne`): cada mensaje lo envía un usuario
- `Message → Booking` (`@ManyToOne`): cada mensaje está asociado a una reserva
- `Payout → Booking` (`@ManyToOne`): cada pago corresponde a una reserva completada

### Queries derivadas
```java
List<House> findByLocation(String location);
List<House> findByPricePerNightLessThan(Double price);
List<House> findByMaxGuestsGreaterThanEqual(Integer guests);
List<Booking> findByStatus(String status);
List<Review> findByRatingGreaterThanEqual(Integer rating);
```
