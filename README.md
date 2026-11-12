# 📱 Android E-Commerce / Social App (Jetpack Compose & Retrofit)

Aplicación nativa para Android desarrollada completamente con **Kotlin** y **Jetpack Compose**. Funciona como un marketplace o red social donde los usuarios autenticados pueden crear, editar, eliminar y visualizar artículos en venta.

El proyecto destaca por su arquitectura moderna, el uso exhaustivo de corrutinas para peticiones asíncronas y la gestión avanzada de estados y navegación dentro del ecosistema de Compose.

### 🚀 Características Principales
- **Sistema de Autenticación (Auth):** Registro, inicio de sesión y cierre de sesión protegidos. Implementación de un `AuthInterceptor` para inyectar dinámicamente el Bearer Token en cada petición HTTP.
- **Gestión de Artículos (CRUD):** Visualización de feed de artículos, creación de nuevos posteos, edición (solo para el autor original) y eliminación mediante gestos (Swipe-to-Dismiss).
- **Subida de Múltiples Imágenes (Multipart):** Implementación compleja para enviar y actualizar imágenes al servidor usando `MultipartBody` de OkHttp, permitiendo al usuario seleccionar archivos locales desde su dispositivo.
- **Navegación Moderna:** Uso de `NavController` para manejar rutas completas, pase de argumentos (IDs) y un `BottomNavigation` dinámico.
- **UI Reactiva:** Construcción de pantallas 100% declarativas con **Jetpack Compose**, incluyendo diálogos de confirmación, paginación horizontal de imágenes (Accompanist Pager) y carga asíncrona de recursos (Coil).

### 🛠️ Stack Tecnológico
- **Lenguaje:** Kotlin
- **Interfaz Gráfica:** Jetpack Compose, Material Design
- **Consumo de API:** Retrofit2, OkHttp3, GsonConverter
- **Asincronía:** Kotlin Coroutines (Dispatchers.IO / Main)
- **Carga de Imágenes:** Coil
- **Almacenamiento Local:** SharedPreferences (TokenManager)

### ⚙️ Estructura de Red (Network Layer)
La aplicación se comunica con un backend (API REST) configurado localmente. Utiliza distintos verbos HTTP (GET, POST, DELETE) y maneja sobrescrituras de métodos (`_method=PUT`) para la correcta actualización de datos y archivos multimedia en el servidor.
