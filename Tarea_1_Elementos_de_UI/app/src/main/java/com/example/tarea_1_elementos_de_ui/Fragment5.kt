package com.example.tarea_1_elementos_de_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.ProgressBar


class Fragment5 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_5, container, false)

        val textView = view.findViewById<TextView>(R.id.textViewInfo)
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        textView.text = "🔥 Oferta especial: Nike Air Max\nDescuento aplicado."
        imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        progressBar.progress = 70 // Simulación: 70% de descuento aplicado

        return view
    }
}
