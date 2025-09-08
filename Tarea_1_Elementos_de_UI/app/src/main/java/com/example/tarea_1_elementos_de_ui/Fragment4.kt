package com.example.tarea_1_elementos_de_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.ListView
import android.widget.ArrayAdapter


class Fragment4 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_4, container, false)

        val listView = view.findViewById<ListView>(R.id.listView)
        val textView = view.findViewById<TextView>(R.id.textViewResult)

        val productos = listOf("Nike Air Max", "Adidas Ultraboost", "Jordan 1", "Puma RS-X", "Yeezy 350")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, productos)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            textView.text = "👟 Seleccionaste: ${productos[position]}"
        }

        return view
    }
}
