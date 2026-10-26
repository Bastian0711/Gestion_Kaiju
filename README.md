# Kaiju Stock

Aplicación móvil Android para el **control de inventario y la trazabilidad de movimientos** de una tienda de coleccionables y artículos Kaiju. Permite saber cuánto stock hay de cada producto, qué movimientos lo cambiaron (entradas, salidas, ajustes y ventas), quién los hizo y cuándo.

Proyecto de la asignatura **DSY1105 · Desarrollo de Aplicaciones Móviles** (Duoc UC), Evaluación Parcial 2. Todos los datos son **ficticios**.

## Integrantes (Grupo 6)

- Bastián Araneda
- Cristóbal Aguilera
- Misael Concha

## Funcionalidades

**Público (sin sesión)**
- Catálogo con buscador. Muestra disponibilidad (Disponible / Pocas unidades / Agotado) sin revelar cantidades exactas.

**Cliente**
- Registro de cuenta con validaciones por campo.
- Catálogo y **carrito de compra**. Al confirmar, la app informa que la compra es presencial (no hay pago en línea ni se descuenta stock).

**Administrador**
- Listado y búsqueda de productos por nombre o código, con estado de stock (Normal, Bajo, Agotado).
- Creación de productos con **foto** (cámara o galería).
- Detalle de producto y **movimientos de stock**: entrada, salida y ajuste por conteo físico, con reglas de validación.
- **Historial por producto (kardex)** con tipo, cantidad, stock resultante, motivo y responsable.
- **Ventas** con varios productos, descuento automático de stock en una transacción, historial y detalle de cada venta.

**General**
- Inicio de sesión con validaciones y bloqueo temporal tras 5 intentos fallidos.
- Contraseñas guardadas con hash (PBKDF2), nunca en texto plano.
- Navegación con roles: cada perfil ve solo las opciones que le corresponden.
- Animaciones en la interfaz (mensajes y elementos que aparecen y desaparecen).

## Recursos nativos

- **Cámara** (con solicitud de permiso en el momento de uso) para fotografiar productos.
- **Galería** del dispositivo para elegir una foto existente.
- **Notificaciones** para avisar cuando stock este bajo

## Tecnologías

- Kotlin y Jetpack Compose (Material 3)
- Arquitectura **MVVM** (ViewModel + StateFlow)
- **Room** para persistencia local
- Navigation Compose
- minSdk 29 (Android 10)

## Estructura del proyecto

Paquete base `com.example.appkaiju.stock`:

| Carpeta | Contenido |
|---|---|
| `model/` | Entidades de Room, enumeraciones y reglas de stock puras (testeables) |
| `data/` | Base de datos, DAOs, contenedor de dependencias, hash de contraseñas, almacén de fotos |
| `repository/` | Acceso a datos y reglas de negocio con transacciones |
| `viewmodel/` | Estado de cada pantalla y validación de formularios |
| `ui/screens/` | Pantallas Compose |
| `ui/components/` | Componentes reutilizables |
| `navigation/` | Rutas y grafo de navegación |

La interfaz no contiene lógica de negocio: los ViewModel validan y los repositorios aplican las reglas de stock.

## Cómo ejecutar

1. Clonar el repositorio:
   ```
   git clone https://github.com/Bastian0711/Gestion_Kaiju.git
   ```
2. Abrir la carpeta del proyecto en **Android Studio** y esperar a que termine el *Gradle Sync*.
3. Conectar un celular Android con depuración USB activada, o iniciar un emulador (Android 10 o superior).
4. Presionar **Run ▶**.

Al primer inicio la app crea automáticamente los datos ficticios (4 categorías, 12 productos y 2 usuarios).

## Usuarios de prueba

| Perfil | Correo | Contraseña |
|---|---|---|
| Administrador | admin@kaiju.cl | Admin123 |
| Cliente | cliente@kaiju.cl | Cliente123 |

También se puede crear un cliente nuevo desde **Registrarse** en la pantalla de login.

## Alcance

Este es un MVP. Quedan fuera de esta entrega: sincronización con servicio remoto, gestión de usuarios, escáner QR y reportes avanzados (ver ERS).
