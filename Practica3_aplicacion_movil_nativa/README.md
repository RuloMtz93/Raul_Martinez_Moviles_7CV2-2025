README – Aplicación Cámara y Micrófono (Kotlin / Android)
Descripción

Aplicación Android desarrollada en Kotlin que integra cámara, grabadora de audio, galerías con búsqueda y ordenamiento, visor y editor de imágenes, reproductor de audio con forma de onda, organizador por álbum y etiquetas, temas Azul y Guinda (con soporte claro y oscuro), y retroalimentación háptica y sonora.

---
Requisitos

Android Studio Giraffe o superior

compileSdk: 36

targetSdk: 36

minSdk: 26

Kotlin + ViewBinding

Probado en Android 8 a 14

---
🔐 Permisos

En Android 13 o superior (API 33+):

READ_MEDIA_IMAGES (fotos)

READ_MEDIA_AUDIO (audios)

Compatibilidad para versiones anteriores:

READ_EXTERNAL_STORAGE

WRITE_EXTERNAL_STORAGE (solo en versiones ≤ 28)

Otros permisos:

CAMERA

RECORD_AUDIO

La aplicación solicita permisos al inicio y de forma contextual cuando es necesario.

---
📦 Dependencias principales (build.gradle)

CameraX (camera-core, camera-camera2, camera-lifecycle, camera-view)

Media3 ExoPlayer (media3-exoplayer, media3-ui)

Glide (caché de miniaturas)

PhotoView (zoom y gestos en imágenes)

Room (metadatos: álbumes, etiquetas y relaciones)

uCrop (recorte de imágenes)

ExifInterface (metadatos EXIF en imágenes)

Material Components

---
🗂 Estructura (archivos clave)

Activities

MainActivity: menú principal.

CameraActivity: captura con CameraX, flash, temporizador, cámara frontal/trasera, filtros, formato JPEG/PNG, EXIF, sonido y vibración al obturador.

AudioRecorderActivity: grabación con MediaRecorder (M4A/AAC), nivel en tiempo real, iniciar, pausar, reanudar y detener; configuración de calidad y duración; guardado en MediaStore (Music/Recordings).

PhotoGalleryActivity: galería de fotos con búsqueda y ordenamiento, selección múltiple (long-press), funciones de organizar y exportar a ZIP.

AudioGalleryActivity: galería de audios con búsqueda y ordenamiento (incluye duración), selección múltiple, organizar y exportar ZIP.

PhotoDetailActivity: visor con zoom, compartir, información EXIF y edición.

AudioPlayerActivity: reproductor con forma de onda, controles de reproducción, renombrar, compartir y eliminar.

OrganizerActivity: asignación de álbumes y etiquetas a los elementos seleccionados.

SettingsActivity: permite cambiar el tema Azul/Guinda en tiempo real.

Adapters / Utils

PhotoAdapter, AudioAdapter

Haptics.kt, Sounds.kt (retroalimentación háptica y sonora)

Layouts

Archivos activity_* e item_* correspondientes, con búsqueda, ordenamiento y botones flotantes para organizar o exportar.

---
🧭 Flujo de uso

Cámara

Tomar foto con vista previa en tiempo real.

Opciones: Flash (Auto/On/Off), Temporizador (0/3/5/10 s), cámara frontal o trasera, filtros (Grayscale, Sepia, Brillo).

Selección de formato JPEG o PNG.

Captura y guardado en MediaStore/DCIM/Camera con metadatos EXIF.

Sonido y vibración de obturador al guardar.

Grabación de audio

Configurar duración y calidad (formato M4A o AAC).

Controles: iniciar, pausar, reanudar y detener.

Muestra nivel en tiempo real.

Guardado en MediaStore/Music/Recordings.

Galería de fotos

Búsqueda por nombre y ordenamiento por fecha o nombre.

Selección múltiple con long-press (opciones: Organizar, Exportar).

Vista detallada con zoom, compartir, información EXIF y edición (recorte, rotación, ajustes).

Galería de audio

Lista con nombre, fecha y duración.

Reproducción o selección múltiple (long-press).

Acciones: Organizar (álbum o etiquetas) y Exportar ZIP.

Exportación

Con elementos seleccionados, elegir “Exportar”.

Se genera un archivo ZIP en Descargas (Export_YYYYMMDD_HHMMSS.zip).

Ofrece abrir o compartir el archivo.

Temas

Dos temas: Azul y Guinda.

Soporte para modo claro y oscuro.

El cambio de tema se aplica sin reiniciar la aplicación.

---
🗃 Almacenamiento

MediaStore: lectura y escritura segura de fotos y audios (scoped storage).

Room: almacenamiento persistente de metadatos (álbumes, etiquetas y relaciones).

Glide: manejo eficiente de caché de miniaturas.

---
⚠️ Limitaciones conocidas

El formato MP3 no es compatible con MediaRecorder nativo (requeriría un pipeline externo, lo cual está justificado en la entrega).

Los metadatos EXIF solo se aplican a imágenes JPEG (PNG no soporta EXIF estándar).

---
Capturas de pantalla
![1](https://github.com/user-attachments/assets/25c0d02b-4e12-49b7-97f1-ec5b1659e0df)
![2](https://github.com/user-attachments/assets/269fe66f-ad4a-4971-80fa-c4c1fc1eac3a)
![3](https://github.com/user-attachments/assets/a7aed140-9d43-44a2-a7b5-bcc8a25d839d)
![4](https://github.com/user-attachments/assets/9003a788-f756-4612-8f01-ab656b309532)
![8](https://github.com/user-attachments/assets/5e245247-1502-4602-82c0-952491c3e3ca)
![7](https://github.com/user-attachments/assets/2b7095b3-6393-4dba-a361-50986c80f01a)
![6](https://github.com/user-attachments/assets/6b881512-52c0-4293-aa31-eeede099f1b0)
![5](https://github.com/user-attachments/assets/c9af8d35-e89f-45d7-a057-5753c1f8f428)

