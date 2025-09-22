# Tarea 1 - Elementos de UI en Android (Kotlin)

Este proyecto proyecto fue desarrollado en Kotlin. Nos muestra el uso de diferentes elementos de la interfaz de usuario mediante 5 fragments.
Se utilizo una barra de navegación para el cambio entre fragments.

---

Estructura
- MainActivity: Es el contenedor de fragments con el menú de navegación inferior.
- 5 Fragments: cada uno muestra un tipo distinto de UI.

---

Descripción de los Fragments

Fragment 1: TextFields (EditText)
- Muestra un campo de texto (`EditText`) donde el usuario puede escribir.
- Incluye un botón para mostrar el texto ingresado.
- **Uso**: Mediante un cuadro de texto podemos buscar la existencia de una marca de tenis

Fragment 2: Botones (Button, ImageButton)
- Incluye un `Button` y un `ImageButton`.
- Al presionarlos, muestran un mensaje en pantalla.
- **Uso**: El botón es para agregar unos tenis al carrito mientras que la imagen de boton es para ver lo que hay en el carrito

Fragment 3: Elementos de selección
- Contiene un `CheckBox`, un `RadioButton` y un `Switch`.
- Muestra en pantalla el estado de cada elemento al cambiar.
- **Uso**: El checkbox es para agregar al carrito unas calcetas, el radiobutton es para seleccionar o no pago contra entrega y el switch para seleccionar envio express

Fragment 4: Listas (ListView)
- Despliega una lista simple de elementos (`ListView`).
- Al seleccionar uno, se muestra el ítem elegido.
- **Uso**: Presenta una lista de los tenis existentes 

Fragment 5: Elementos de información
- Incluye un `TextView`, un `ImageView` y un `ProgressBar`.
- Se usan para mostrar información visual y estados de progreso.
- **Uso**: Informar al usuario sobre un descuento aplicado a un producto

---

Tecnologías utilizadas
- Lenguaje: **Kotlin**
- Arquitectura: **Fragments** con `BottomNavigationView`
- Librería de UI: **Material Components**

---

Autor
Martínez Pérez Raúl
