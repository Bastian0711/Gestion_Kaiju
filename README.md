# Kaiju Stock

App Android para controlar el inventario de una tienda de productos Kaiju. Permite ver el stock de cada producto, registrar entradas, salidas, ajustes y ventas, y revisar quién hizo cada movimiento.

Proyecto de DSY1105 Desarrollo de Aplicaciones Móviles, Duoc UC. Evaluación Parcial 4. Los datos son ficticios.

## Integrantes (Grupo 6)

- Bastián Araneda
- Cristóbal Aguilera
- Misael Concha

## Funcionalidades

- Login con validaciones y bloqueo de 60 segundos después de 5 intentos fallidos.
- Registro de clientes.
- Cada rol ve solo sus opciones (administrador o cliente).
- Catálogo con buscador y carrito de compras para el cliente.
- Productos con foto (cámara o galería).
- Movimientos de stock: entrada, salida y ajuste.
- Kardex de cada producto.
- Ventas con descuento de stock, historial y detalle.
- Notificación cuando un producto queda con stock bajo.
- Indicadores económicos (dólar, euro, UF y UTM) desde una API externa y precio de referencia en dólares.
- Gestión de usuarios (listar, crear, editar y desactivar) usando nuestro microservicio.

## Tecnologías

- Kotlin + Jetpack Compose
- MVVM (ViewModel y StateFlow)
- Room
- Retrofit + Gson
- Kotest, MockK y JaCoCo para las pruebas

## Endpoints

**API externa (mindicador.cl)**

- GET `https://mindicador.cl/api` : indicadores económicos del día

**Microservicio de usuarios (Spring Boot)**

Repositorio: https://github.com/MisaelCtrlv/backendfs_dos

URL base: `http://IP_ELASTICA_EC2:8081/` (se cambia en `RetrofitInstance.kt`)

- POST `/api/v1/auth/login` : iniciar sesión y obtener el token
- GET `/api/v1/usuarios` : listar usuarios
- POST `/api/v1/usuarios` : crear usuario
- PUT `/api/v1/usuarios/{id}` : editar usuario
- DELETE `/api/v1/usuarios/{id}` : desactivar usuario

Usuario admin del microservicio: admin@maestros.cl / Admin123!

## Cómo ejecutar

1. Clonar el repositorio: `git clone https://github.com/Bastian0711/Gestion_Kaiju.git`
2. Abrirlo en Android Studio y esperar el Gradle Sync.
3. Conectar un celular con depuración USB o usar un emulador.
4. Darle a Run.

Usuarios de prueba de la app:

- Administrador: admin@kaiju.cl / Admin123
- Cliente: cliente@kaiju.cl / Cliente123

## Pruebas

Hicimos 138 pruebas unitarias con Kotest y MockK (modelos, repositorios y ViewModels) y 6 pruebas de interfaz con Compose. La cobertura del código lógico es de 96%.

- Pruebas unitarias: `gradlew testDebugUnitTest`
- Cobertura: `gradlew jacocoTestReport`
- Pruebas de interfaz: `gradlew connectedDebugAndroidTest`

![Pruebas unitarias](docs/capturas/pruebas_unitarias.png)
![Cobertura](docs/capturas/cobertura_jacoco.png)
![Pruebas de interfaz](docs/capturas/pruebas_ui.png)

## APK firmado

El APK firmado está en la carpeta `apk/` y la llave en `keystore/kaiju-release.jks`. La firma está configurada en `app/build.gradle.kts`. Las contraseñas van en `keystore.properties`, que no subimos al repositorio (hay un archivo `.example` de guía).

Para generarlo: `gradlew assembleRelease`

![Llave jks](docs/capturas/jks.png)
![APK firmado](docs/capturas/apk_firmado.png)

## Links

- ERS: https://drive.google.com/drive/folders/1A5b_aJWwqVYcsd6SF70FAP3Yk5CCPovK?usp=sharing
