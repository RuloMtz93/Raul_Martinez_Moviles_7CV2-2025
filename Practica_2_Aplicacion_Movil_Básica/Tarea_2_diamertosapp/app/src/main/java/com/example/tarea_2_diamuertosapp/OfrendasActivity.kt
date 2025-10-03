package com.example.tarea_2_diamuertosapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OfrendasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ofrendas)

        // Referencias a ViewPager2 y TabLayout
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)

        // Adaptador que controla los fragments
        val adapter = OfrendasDeslizar(this)
        viewPager.adapter = adapter

        // Configura los tabs con los títulos de cada fragment
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Calaveritas"
                1 -> tab.text = "Pan de Muerto"
                2 -> tab.text = "Papel Picado"
            }
        }.attach()
    }
}