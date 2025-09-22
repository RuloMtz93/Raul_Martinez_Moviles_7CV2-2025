package com.example.tarea_1_elementos_de_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.ImageView


class Fragment2 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_2, container, false)

        val textView = view.findViewById<TextView>(R.id.textView)
        val button = view.findViewById<Button>(R.id.buttonNormal)
        val imageButton = view.findViewById<ImageButton>(R.id.imageButton)

        button.setOnClickListener {
            textView.text = "🛒 Tenis agregado al carrito"
        }

        imageButton.setOnClickListener {
            textView.text = "👟 Vista previa del producto"
            imageButton.setImageResource(android.R.drawable.ic_menu_gallery) // Simulación de foto
        }

        return view
    }
}
