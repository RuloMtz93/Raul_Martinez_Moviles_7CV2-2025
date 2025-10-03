package com.example.tarea_2_diamuertosapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.tarea_2_diamuertosapp.OfrendasActivity
import com.example.tarea_2_diamuertosapp.R

class BotonVerOfrendasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout del fragment
        val view = inflater.inflate(R.layout.boton_ver_ofrendas_fragment, container, false)

        // Referencia al botón
        val btnIrAOfrendas: Button = view.findViewById(R.id.btn_ir_a_ofrendas)

        // Acción al dar clic
        btnIrAOfrendas.setOnClickListener {
            val intent = Intent(requireContext(), OfrendasActivity::class.java)
            startActivity(intent)

            // Animación entre activities (fade in / fade out)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        return view
    }
}
