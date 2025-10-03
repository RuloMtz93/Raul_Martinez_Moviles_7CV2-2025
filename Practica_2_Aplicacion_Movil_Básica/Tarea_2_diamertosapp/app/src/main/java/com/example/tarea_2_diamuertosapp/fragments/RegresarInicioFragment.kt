package com.example.tarea_2_diamuertosapp.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.tarea_2_diamuertosapp.MainActivity
import com.example.tarea_2_diamuertosapp.R

class RegresarInicioFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.regresar_inicio_fragment, container, false)

        val button = view.findViewById<Button>(R.id.btnRegresarInicio)
        button.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
