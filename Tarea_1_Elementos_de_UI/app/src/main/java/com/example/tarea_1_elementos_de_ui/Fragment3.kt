package com.example.tarea_1_elementos_de_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Switch


class Fragment3 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_3, container, false)

        val textView = view.findViewById<TextView>(R.id.textViewResult)
        val checkBox = view.findViewById<CheckBox>(R.id.checkBox)
        val radioButton = view.findViewById<RadioButton>(R.id.radioButton)
        val switch = view.findViewById<Switch>(R.id.switchButton)

        checkBox.text = "Añadir calcetas (+$5)"
        radioButton.text = "Pago contra entrega"
        switch.text = "Envío exprés"

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            textView.text = if (isChecked) "✅ Calcetas añadidas" else "❌ Sin calcetas"
        }

        radioButton.setOnCheckedChangeListener { _, isChecked ->
            textView.text = if (isChecked) "💵 Pago contra entrega seleccionado" else ""
        }

        switch.setOnCheckedChangeListener { _, isChecked ->
            textView.text = if (isChecked) "🚚 Envío exprés activado" else "📦 Envío normal"
        }

        return view
    }
}
