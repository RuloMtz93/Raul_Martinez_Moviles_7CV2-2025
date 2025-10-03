# Día de Muertos App

## Descripción
"Día de Muertos App" es una aplicación educativa y cultural desarrollada en Android que permite a los usuarios conocer los elementos más representativos del Día de Muertos y su historia. La aplicación está diseñada con un enfoque visual atractivo y dinámico, incluyendo soporte para **modo claro y oscuro**.

---

## Funcionalidades principales

1. **Pantalla de bienvenida (MainActivity)**
   - Muestra un título alusivo al Día de Muertos.
   - Permite al usuario seleccionar entre **modo claro y oscuro** mediante un switch.
   - Contiene un botón que lleva al usuario al apartado de **Ofrendas**.

2. **Ofrendas (OfrendasActivity)**
   - Compuesta por un **ViewPager2** con **3 fragments** deslizables:
     - **Calaveritas de Azúcar**
     - **Pan de Muerto**
     - **Papel Picado**
   - Cada fragment incluye:
     - Una imagen alusiva.
     - Información descriptiva sobre el elemento.
     - Botón para acceder a la **Historia del Día de Muertos**.

3. **Historia (CulturaActivity)**
   - Muestra la historia completa del Día de Muertos en un **ScrollView** para lectura completa.
   - Texto adaptativo a **modo claro y oscuro**.
   - Contiene un botón al final para **regresar a la pantalla principal**.
   - Mantiene una experiencia de usuario consistente y clara, con imagen alusivas y diseño uniforme con el resto de la app.

---

## Implementación de Temas con SharedPreferences

La aplicación permite al usuario cambiar entre **modo claro y oscuro** y mantener esta preferencia incluso al cerrar la app.  

**Cómo se implementó:**
- Se definieron colores y estilos para **modo claro** y **modo oscuro** en los archivos `themes.xml` y `colors.xml`.
- Se colocó un **Switch** en la pantalla principal (MainActivity) para permitir al usuario alternar entre los temas.
- Cuando el usuario activa o desactiva el Switch, se guarda su preferencia en `SharedPreferences` como un valor booleano (`isDarkModeEnabled`).
- Al iniciar cualquier Activity, antes de `setContentView()`, se lee el valor guardado en `SharedPreferences` y se aplica el tema correspondiente (`R.style.Theme_Claro` o `R.style.Theme_Oscuro`).

**Cómo usar el selector de tema:**
1. Abrir la aplicación y dirigirse a la pantalla de bienvenida.
2. Utilizar el **Switch** para activar el modo oscuro (ideal para lectura nocturna) o modo claro (para el día).
3. La aplicación recordará tu elección incluso después de cerrarla y volver a abrirla.

---
## Características técnicas

- Implementación de **ViewPager2** para deslizar entre fragments.
- Uso de **FragmentContainerView** para modularidad y reutilización de fragments.
- Adaptación de colores y textos a **tema claro y oscuro** mediante `SharedPreferences`.
- Scrollable TextView para textos largos y accesibilidad.
- Compatible con la mayoría de dispositivos Android modernos.

---

## Futuras mejoras

- Reproducción de música ambiental durante la navegación en la app.
- Animaciones y transiciones más dinámicas entre fragments.
- Posibilidad de agregar más elementos culturales y enlaces multimedia.
- Implementación de preferencias de usuario avanzadas, como volumen de música y tamaño de letra.

---

## Capturas de pantalla

![1](https://github.com/user-attachments/assets/85b3fa4b-d4af-4d8c-a59e-19e7f8cd1af5)
![2](https://github.com/user-attachments/assets/5a6eba45-07de-406a-a1f7-e584acaf380a)
![3](https://github.com/user-attachments/assets/cbf6aeab-f456-4e1e-9b2b-158d7b9ffa00)
![4](https://github.com/user-attachments/assets/cef107bf-aed4-4f51-b436-f7353522d349)
![5](https://github.com/user-attachments/assets/7d72aa60-b011-4e35-ab91-1e406a9d8012)
![6](https://github.com/user-attachments/assets/7d69ee11-4c9d-4267-9c85-9af602416960)

---

## Video funcionamiento


https://github.com/user-attachments/assets/e61b8b9c-3929-405b-aba5-1e26394e3d1b






