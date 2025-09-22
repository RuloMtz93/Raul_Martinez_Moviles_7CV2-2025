package com.example.tarea_1_elementos_de_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment

class Fragment1 : Fragment() {

    private val tenisDisponibles = listOf("Air Max", "Ultraboost", "Jordan", "Yeezy", "Puma RS-X")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_1, container, false)

        val input = view.findViewById<EditText>(R.id.editText)
        val button = view.findViewById<Button>(R.id.buttonSubmit)
        val result = view.findViewById<TextView>(R.id.textResult)

        button.setOnClickListener {
            val busqueda = input.text.toString().trim()
            if (busqueda.isEmpty()) {
                result.text = "Por favor escribe un modelo de tenis."
            } else if (tenisDisponibles.any { it.equals(busqueda, ignoreCase = true) }) {
                result.text = "✅ El modelo '$busqueda' está disponible."
            } else {
                result.text = "❌ Lo sentimos, no tenemos '$busqueda'."
            }
        }

        return view
    }
}
