# 🏬 Práctica 1: "Instalación y Funcionamiento de los Entornos Móviles"

## 📌 Descripción del Proyecto
Esta práctica tiene como objetivo desarrollar una aplicación móvil que demuestre el uso de **Activities y Fragments** en Android nativo (Kotlin) y su equivalente en **Flutter**, mostrando diferentes **elementos de interfaz de usuario (UI)** de manera interactiva.

La app consta de **cinco secciones principales**, cada una dedicada a un tipo de elemento de UI:

1. **TextFields (EditText)**
2. **Botones (Button, ImageButton)**
3. **Elementos de selección (CheckBox, RadioButton, Switch)**
4. **Listas (RecyclerView / ListView)**
5. **Elementos de información (TextView, ImageView, ProgressBar)**

Se implementó **navegación** entre los fragments/pantallas y cada sección es interactiva, permitiendo al usuario probar las funcionalidades directamente.

---

## 🛠 Instalación de Herramientas

Antes de ejecutar la app, asegúrate de tener instaladas y configuradas las siguientes herramientas:

- **Android Studio**: IDE principal para desarrollo Android. Configura el emulador para probar las apps.
- **JDK (Amazon Corretto u OpenJDK)**: Necesario para compilar programas en Java/Kotlin.
- **Maven**: Para automatizar la construcción de proyectos y gestionar dependencias.
- **Git y GitHub**: Control de versiones y almacenamiento remoto de los proyectos.
- **Docker**: Para gestión de bases de datos si es necesario.
- **Node.js**: Para herramientas adicionales del proyecto.
- **Flutter**: Para ejecutar la versión en Flutter de la aplicación.

---

## 📁 Estructura del Proyecto
  **Kotlin / Android Studio:**
  
    Tarea_UI_Android/
    ├─ app/
    │ ├─ src/main/java/com/example/app/
    │ │ ├─ MainActivity.kt
    │ │ ├─ Fragment1.kt
    │ │ ├─ Fragment2.kt
    │ │ └─ ...
    │ └─ res/
    │ ├─ layout/
    │ └─ drawable/
    ├─ build.gradle
    └─ ...
    
  **Flutter / Visual Studio:**
  
     tarea_ui_flutter/
     ├─ lib/
     │ ├─ main.dart
     │ └─ screens/
     │ ├─ search_screen.dart
     │ ├─ actions_screen.dart
     │ └─ ...
     │ └─ images/
     │ ├─ airmax.png
     │ └─ ultraboost.png
     ├─ pubspec.yaml
     └─ ...
     
     ---

## 🚀 Ejecución del Proyecto

### Android Studio (Kotlin)

1. Abre el proyecto en **Android Studio**.
2. Conecta un dispositivo físico o configura un emulador.
3. Presiona **Run** (▶) para compilar y ejecutar la aplicación.
4. Navega entre los fragments usando el menú inferior y prueba los elementos interactivos.

### Flutter

1. Abre el proyecto en **Visual Studio Code** o Android Studio.
2. Asegúrate de que tu dispositivo esté en **modo desarrollador** o utiliza un emulador.
3. Ejecuta en terminal:
   
   flutter pub get
   flutter run

5. Navega entre las pantallas usando la barra inferior de navegación y prueba la funcionalidad de cada sección.

---

## 🎨 Funcionalidades

- 🔍 **Buscar modelos de tenis** (TextField)
- 🛒 **Agregar al carrito y vista rápida de imágenes** (Botones / ImageButton)
- ✅ **Seleccionar opciones de compra** (CheckBox, RadioButton, Switch)
- 📃 **Visualizar catálogo de productos** (ListView)
- ℹ️ **Ver detalles de producto y disponibilidad** (ImageView, ProgressBar)

Cada sección incluye explicaciones breves y demostraciones interactivas.

---

## ⚠️ Dificultades y Soluciones

- 📌 **Assets en Flutter:** Al inicio las imágenes no se cargaban. Solución: colocar las imágenes en `assets/images/` y registrar correctamente la ruta en `pubspec.yaml`.
- 📌 **Widget_test.dart:** Se subrayaba `MyApp` como clase no encontrada. Solución: se cambió a `TiendaTenisApp` según la definición en `main.dart`.
- 📌 **Gestión de fragments en Kotlin:** Requiere cuidado en la navegación y manejo de estados para que los cambios en un fragment se reflejen en otros.

---

## 💡 Hallazgos

- La implementación de **Activities y Fragments** permite organizar mejor la interfaz y reutilizar componentes.
- Flutter facilita crear aplicaciones multiplataforma con la misma lógica y estructura que Android nativo, pero con un solo código base.
- Las pruebas con **SnackBar** y **Widgets interactivos** ayudan a que el usuario entienda la funcionalidad sin necesidad de instrucciones externas.

---

## 📸 Evidencias

Herramientas Instaladas

  Android Studio
  
  <img width="1366" height="725" alt="image" src="https://github.com/user-attachments/assets/7da49723-1744-4680-a61c-485b0e13e760" />

  Flutter
  
  <img width="978" height="511" alt="image" src="https://github.com/user-attachments/assets/df226803-3c56-431b-867b-1ad4dd58beb7" />

  Github Desktop
  
  <img width="1366" height="729" alt="image" src="https://github.com/user-attachments/assets/154b2631-324e-4a85-95df-712ea9a86fac" />

---

  Aplicación Android Studio
  
  ![1](https://github.com/user-attachments/assets/0872c93c-4b3e-4227-9f27-7f970040f4f4)
  ![2](https://github.com/user-attachments/assets/277ad9a0-3c13-4392-aa3a-12ce61189beb)
  ![3](https://github.com/user-attachments/assets/f6309279-2d83-4776-895e-91b3f9f49cbd)
  ![4](https://github.com/user-attachments/assets/c9cd7095-87da-490d-8b51-3b993f72c4d9)
  ![5](https://github.com/user-attachments/assets/b5d9239c-2890-4214-9294-3e359147310b)

---

  Aplicación Flutter
  
  ![1](https://github.com/user-attachments/assets/a7632ad2-df00-4b6c-aa89-4b40bb192091)
  ![2](https://github.com/user-attachments/assets/d0e901b8-ce38-4073-a313-ee081f17a0f9)
  ![3](https://github.com/user-attachments/assets/ac5dbda8-2807-4b7b-a932-72010f80cdb4)
  ![4](https://github.com/user-attachments/assets/79ad7f83-2c75-48bf-8501-42853f15bad7)
  ![5](https://github.com/user-attachments/assets/e780bfdd-d519-4e67-8dc0-c036355f4d8b)


  






