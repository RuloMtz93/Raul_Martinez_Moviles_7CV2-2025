README – Aplicación Cámara y Micrófono (Kotlin / Android)
📌 Descripción

App Android (Kotlin) que integra cámara, grabadora de audio, galerías con búsqueda/orden, visor/editor de imágenes, reproductor de audio con forma de onda, organizador por álbum/etiquetas, temas Azul/Guinda (con soporte claro/oscuro), feedback háptico/sonoro
---
🧱 Requisitos

Android Studio Giraffe o superior
compileSdk: 36
targetSdk: 36
minSdk: 26
Kotlin + ViewBinding
Probado en Android 8–14
---
🔐 Permisos

En Android 13+ (API 33+):
READ_MEDIA_IMAGES (fotos)
READ_MEDIA_AUDIO (audios)
Compatibilidad para versiones previas:
READ_EXTERNAL_STORAGE
WRITE_EXTERNAL_STORAGE (sólo <= 28)
Otros:
CAMERA
RECORD_AUDIO
La app solicita permisos en el inicio y de forma contextual cuando aplica.
---
📦 Dependencias principales (build.gradle)

CameraX (camera-core, camera-camera2, camera-lifecycle, camera-view)
Media3 ExoPlayer (media3-exoplayer, media3-ui)
Glide (caché miniaturas)
PhotoView (zoom/gestos en imágenes)
Room (metadatos: álbumes, etiquetas, relaciones)
uCrop (recorte)
ExifInterface (EXIF imágenes)
Material Components
---
🗂 Estructura (archivos clave)

Activities
MainActivity: menú principal.
CameraActivity: captura con CameraX, flash, temporizador, frontal/trasera, filtros, formato JPEG/PNG, EXIF, obturador sonoro + háptico.
AudioRecorderActivity: grabación MediaRecorder (M4A/AAC), nivel en tiempo real, iniciar/pausar/reanudar/detener, calidad y duración configurables, guardado en MediaStore (Music/Recordings).
PhotoGalleryActivity: galería de fotos con búsqueda/orden, selección múltiple (long-press), Organizar, Exportar ZIP.
AudioGalleryActivity: galería de audios con búsqueda/orden (incluye duración), selección múltiple, Organizar, Exportar ZIP.
PhotoDetailActivity: visor con zoom + Compartir, Info EXIF, Editar.
AudioPlayerActivity: reproductor con forma de onda, play/pausa/seek, renombrar/compartir/eliminar.
OrganizerActivity: asignar álbumes/etiquetas a la selección.
SettingsActivity (si la incluyes): cambio de Tema Azul/Guinda al vuelo.
Adapters / Utils
PhotoAdapter, AudioAdapter
Haptics.kt, Sounds.kt: feedback háptico y sonido de obturador.
Layouts
activity_* y item_* correspondientes (incluyen búsqueda/orden y FABs “Organizar/Exportar”).
---
🧭 Flujo de uso

Cámara
Tomar Foto → Preview en tiempo real.
Opciones: Flash Auto/On/Off, Temporizador (0/3/5/10), Cámara frontal/trasera, Filtro (Grayscale/Sepia/Brillo).
Formato: elige JPEG/PNG → captura → se guarda en MediaStore/DCIM/Camera con EXIF.
Sonido de obturador + háptico al guardar.
Grabación de audio
Grabar Audio → configura duración y calidad; formato M4A (AAC/MP4) o AAC (.aac).
Controles: iniciar, pausar, reanudar, detener; nivel en tiempo real.
Guarda en MediaStore/Music/Recordings.
Galería de fotos
Búsqueda por nombre y orden (fecha/nombre).
Long-press para selección múltiple: aparecen Organizar y Exportar.
Abrir una foto para ver zoom, Compartir, Info EXIF, Editar (recorte/rotación/ajustes básicos).
Galería de audio
Lista con nombre, fecha y duración.
Play (si no hay selección) o selección múltiple (long-press).
Acciones: Organizar (álbum/etiquetas), Exportar ZIP.
Exportar selección
Con elementos seleccionados, pulsa Exportar → genera ZIP en Descargas (Export_YYYYMMDD_HHMMSS.zip) y ofrece abrir/compartir.
Temas
Azul y Guinda, con modo claro/oscuro. El cambio se aplica en caliente (sin reiniciar app).
---
🗃 Almacenamiento

MediaStore: escritura/lectura segura de fotos y audios (scoped storage).
Room: metadatos persistentes (álbumes, etiquetas, relaciones item↔etiquetas).
Glide: caché de miniaturas eficiente.
---
⚠️ Limitaciones conocidas

MP3: no soportado por MediaRecorder nativamente (requeriría pipeline externo — justificado en entrega)
EXIF: sólo para JPEG (PNG no almacena EXIF estándar).
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

