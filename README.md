# IT DeskTracker


<img width="169" height="212" alt="02_logo_vertical_navy_transparente" src="https://github.com/user-attachments/assets/e2b177c7-60b0-4827-9883-f22a1467f4e3" />


Aplicación de escritorio para gestionar el soporte técnico de un área de IT, desarrollada con **Java, JavaFX, FXML y SceneBuilder**. Centraliza el ingreso de equipos, el seguimiento de los servicios y la agenda del equipo técnico en una interfaz moderna con estilo *liquid glass*.

## Características

- **Inicio de sesión por técnico** con roles (técnico y administrador).
- **Panel principal** con indicadores en vivo: dispositivos pendientes, en proceso, completados y citas del día, además de accesos rápidos a cada módulo.
- **Dispositivos:** registro de equipos con foto de referencia, consulta, edición e impresión.
- **Tickets de servicio:** creación y seguimiento por estado (pendiente, en proceso, completado) con código único.
- **Impresión de tickets** listos para entregar al cliente.
- **Calendario:** agenda del día y de la semana para las citas de servicio.
- **Gestión de personal:** alta y administración de cuentas, solo para administradores.
- **Alertas y confirmaciones** personalizadas, con animaciones.

## Diseño

Interfaz inspirada en *liquid glass*: paneles translúcidos con reflejos y bordes luminosos sobre un fondo fotográfico, definida en una única hoja de estilos (`styles.css`). Todas las vistas se pueden editar visualmente con SceneBuilder.
<img width="673" height="990" alt="image" src="https://github.com/user-attachments/assets/76e92b07-fdf0-4f18-ba64-745c95f70f1b" />
<img width="1918" height="1198" alt="image" src="https://github.com/user-attachments/assets/89af19c2-0e5a-49b2-9ac5-8ff2ffc692a6" />
<img width="1917" height="1197" alt="image" src="https://github.com/user-attachments/assets/ab741f2b-84c4-403f-8255-4b6761ece718" />
<img width="122" height="121" alt="10_icono_gris_transparente" src="https://github.com/user-attachments/assets/513c30b9-eb45-4bc1-89fa-68a0ae2be00c" />


## Tecnologías

- Java 26
- JavaFX 27 + FXML + SceneBuilder
- CSS de JavaFX
- Base de datos relacional (acceso mediante capa de servicios)

## Estructura del proyecto
