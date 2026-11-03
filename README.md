[README.md](https://github.com/user-attachments/files/34324217/README.md)
# IT Desk Tracker


Aplicación de escritorio para registrar los equipos que ingresan a un área de soporte técnico, darles seguimiento con tickets y agendar las citas de los técnicos. Está hecha en Java con JavaFX y guarda todo en una base de datos SQLite local.


<img width="190" height="190" alt="07_icono_navy_transparente" src="https://github.com/user-attachments/assets/e86d9f0b-752d-46a1-8546-268f49be37f2" />
<img width="169" height="212" alt="02_logo_vertical_navy_transparente" src="https://github.com/user-attachments/assets/53f416ee-7db3-46cc-bc13-f536a4a10b7c" />


## Módulos        <img width="50" height="50" alt="login-ventana" src="https://github.com/user-attachments/assets/72d5c30f-d85f-4c86-a679-d13b383d989f" />


**Inicio de sesión.** Cada técnico entra con correo y contraseña. Si la base de datos no tiene técnicos, la primera pantalla pide crear la cuenta del administrador.

**Personal.** Alta, edición y desactivación de técnicos. Hay dos roles: Administrador y Técnico. Una cuenta no puede desactivarse a sí misma.
<img width="1550" height="995" alt="image" src="https://github.com/user-attachments/assets/406031c7-3a63-4336-a052-de3bf7fbaa35" />

**Dispositivos.** Registro del equipo que deja el cliente.
<img width="1241" height="896" alt="image" src="https://github.com/user-attachments/assets/726339a4-3e44-4b3e-85d0-0eddb7d83a53" />


<img width="50" height="50" alt="rem_feliz" src="https://github.com/user-attachments/assets/383a1f00-08f4-4410-91ab-aab065fbce10" />

| Campo | Obligatorio |
|---|---|
| Tipo, marca, modelo | Sí |
| Serie | No |
| Cliente, teléfono | Sí |
| Correo | No |
| Problemática | Sí |
| Diagnóstico, accesorios, notas | No |
| Imagen del equipo | No |
| Fecha estimada de entrega | No |

Al registrar un dispositivo se guarda también el técnico que lo recibió y la fecha de ingreso.

**Tickets.** Cada dispositivo tiene un ticket con descripción, estado y folio. Los estados son `Pendiente`, `En proceso` y `Completado`; al completarse se guarda la fecha. El folio se genera solo con el formato `NEX-AAAA-NNNN` (por ejemplo `NEX-2026-0001`), donde AAAA es el año y NNNN un consecutivo de cuatro dígitos.
<img width="1917" height="1136" alt="image" src="https://github.com/user-attachments/assets/540fd832-f261-4ed9-a9c3-fb2b5c5ff71d" />

**Impresión de tickets.** Por cada dispositivo se imprimen dos tickets: uno para el equipo y otro para el cliente. Cada ticket mide 396 × 612 pt (media carta) y los dos salen juntos en una hoja carta horizontal. Antes de imprimir se muestra una vista previa. Cada impresión se registra con el técnico y la fecha, y el ticket queda marcado como impreso.
<img width="667" height="647" alt="image" src="https://github.com/user-attachments/assets/51438ef3-0b08-4ca8-9c9f-6338feab97b7" />
<img width="1002" height="841" alt="image" src="https://github.com/user-attachments/assets/e7db8f4d-8e4e-4707-96ac-fbab59c254e0" />
<img width="522" height="370" alt="image" src="https://github.com/user-attachments/assets/f98d7553-e7aa-4aae-b9ec-ed71578df197" />

**Calendario.** Citas por técnico con título, descripción, fecha y hora. Se pueden marcar como completadas y reabrir.
<img width="1917" height="1145" alt="image" src="https://github.com/user-attachments/assets/16b8bacd-5645-48f4-a568-5d895383d094" />

**Dashboard.** Muestra el técnico en sesión con su rol, el número de tickets pendientes, en proceso y completados, y las citas del día.
<img width="960" height="340" alt="IT_Desk_Tracker_50fps" src="https://github.com/user-attachments/assets/f60f6eb4-d072-4fa5-a82b-d6b9744aabab" />

## Validaciones y seguridad

- Contraseña de mínimo 8 caracteres, guardada con PBKDF2WithHmacSHA256.
- Correo con formato `usuario@dominio.ext`.
- Teléfono de 7 a 20 caracteres: dígitos, `+`, `(`, `)`, `-` y espacios.
- La descripción del ticket tiene un máximo de 255 caracteres.

## Base de datos

SQLite con claves foráneas activadas y modo WAL. El esquema se crea automáticamente al abrir la aplicación por primera vez.

| Tabla | Contenido |
|---|---|
| `tecnico` | Nombre, correo único, contraseña, rol (`ADMIN` o `TECNICO`) y si está activo |
| `dispositivo` | Datos del equipo y del cliente, estado, fechas y técnico que lo recibió |
| `ticket` | Folio único, descripción, estado, fechas de ingreso y de completado, estado de impresión |
| `impresion_ticket` | Registro de cada impresión: ticket, tipo (`EQUIPO` o `CLIENTE`), técnico y fecha |
| `cita` | Técnico, título, descripción, fecha, hora y si está completada |

Relaciones: un `ticket` pertenece a un `dispositivo` y se elimina con él; `impresion_ticket` se elimina con su `ticket`; las `cita` se eliminan con su técnico; al eliminar un técnico, los dispositivos y tickets que atendió conservan su registro sin técnico asignado.

### Ubicación de los datos

| Qué | Ruta en Windows |
|---|---|
| Base de datos | `%APPDATA%\ITDeskTracker\it-desk-tracker.db` |
| Imágenes de equipos | `%APPDATA%\ITDeskTracker\imagenes\` |

Si no existe `APPDATA`, se usa `.itdesktracker` dentro de la carpeta del usuario. La ruta de la base de datos se puede cambiar con la propiedad de JVM `-Ditdesk.db=<ruta>`.

## Tecnologías

| Componente | Versión |
|---|---|
| JDK | 26 |
| JavaFX | 27.0.1 |
| sqlite-jdbc | 3.53.4.0 |
| Construcción | Apache Ant (proyecto de NetBeans) |

## Arquitectura

Capas: `Controller → Service → Repository → DB`. Las vistas son archivos FXML con hojas de estilo CSS, con tema oscuro.

```
src/org/angelml/system/
├── MainApp.java
├── config/       AppConfig: nombre, rutas y prefijo del folio
├── DB/           conexión y creación del esquema
├── model/        Tecnico, Dispositivo, Ticket, Cita, Rol, EstadoServicio
├── repository/   acceso a datos
├── service/      reglas de negocio
├── controller/   un controlador por vista
├── utils/        alertas, escenas, sesión, validaciones, tickets
└── resources/    view (FXML), styles (CSS), media
```
