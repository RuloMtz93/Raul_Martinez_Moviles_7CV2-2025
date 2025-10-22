Conecta 4
---

Juego de Conecta 4 para dos jugadores locales (“pass & play”), desarrollado con Jetpack Compose y arquitectura MVVM.
Cumple con: sistema de turnos, validación de movimientos, detección de victoria/empate, marcador, reinicio de partida, e interfaz intuitiva con retroalimentación visual.

---

🕹️ Reglas del juego

Dos jugadores: Rojo y Amarillo (o Jugador 1 y 2).

Tablero de 7 columnas x 6 filas.

En cada turno, el jugador deja caer una ficha en una columna válida (no llena).

Gana quien forme 4 en línea (horizontal, vertical o diagonal).

Empate si el tablero se llena sin ganador.

Puedes reiniciar la partida en cualquier momento.

---

✨ Funcionalidades

Sistema de turnos: alternancia automática con indicador visual de quién juega.

Validación de movimientos: bloquea columnas llenas y mantiene turno si el movimiento es inválido.

Detección de victoria/empate: cálculo inmediato tras cada jugada.

Marcador: conteo de victorias por jugador (opcional: empates).

Reinicio de partida: botón de “Reiniciar” sin cerrar la app.

UI Compose + Material 3: colores, animaciones de selección y resaltado de victoria.

Accesibilidad: textos claros, toques grandes, soporte de rotación.

Arquitectura MVVM: estado en ViewModel con StateFlow, UI declarativa.

---

🧰 Requisitos del sistema

Android Studio: Giraffe / Hedgehog / Iguana o superior (cualquiera reciente).

Versión mínima de Android (minSdk): 24 🔧

compileSdk/targetSdk: 35 🔧

Kotlin / Compose Compiler Plugin: 2.0.20 🔧

Android Gradle Plugin (AGP): 8.7.1 🔧

Gradle wrapper: 8.9 🔧

---

📸 Capturas

![Conecta2](https://github.com/user-attachments/assets/e2051de0-a88c-40ef-b6e0-81e16bfad1fd)
![Conecta1](https://github.com/user-attachments/assets/027fcdbb-f4de-4c74-8abd-93a88c914386)
![Conecta4](https://github.com/user-attachments/assets/5a14cf35-e544-42d9-b9fc-410ea4e53d52)
![Conecta3](https://github.com/user-attachments/assets/b2ec1eeb-4450-49ae-bdf8-187f3209e701)

---

👤 Autores

Nombre: Hernández Pérez Diego Francisco / Martínez Pérez Raúl

Materia: Desarrollo de aplicaciones móviles nativas

Profesor/Grupo: 7CV2


