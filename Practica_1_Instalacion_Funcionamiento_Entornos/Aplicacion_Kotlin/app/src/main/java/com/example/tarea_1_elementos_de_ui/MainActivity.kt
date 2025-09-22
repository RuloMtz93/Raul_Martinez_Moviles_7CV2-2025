package com.example.tarea_1_elementos_de_ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.tarea_1_elementos_de_ui.Fragment1
import com.example.tarea_1_elementos_de_ui.Fragment2
import com.example.tarea_1_elementos_de_ui.Fragment3
import com.example.tarea_1_elementos_de_ui.Fragment4
import com.example.tarea_1_elementos_de_ui.Fragment5


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Fragment inicial
        if (savedInstanceState == null) {
            loadFragment(Fragment1())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_fragment1 -> loadFragment(Fragment1())
                R.id.nav_fragment2 -> loadFragment(Fragment2())
                R.id.nav_fragment3 -> loadFragment(Fragment3())
                R.id.nav_fragment4 -> loadFragment(Fragment4())
                R.id.nav_fragment5 -> loadFragment(Fragment5())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}