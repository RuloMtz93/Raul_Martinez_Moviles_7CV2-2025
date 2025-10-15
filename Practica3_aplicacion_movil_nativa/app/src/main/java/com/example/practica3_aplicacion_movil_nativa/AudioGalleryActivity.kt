package com.example.practica3_aplicacion_movil_nativa

import android.content.ContentUris
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AudioGalleryActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AudioAdapter

    // Visible para adapter
    private val audioList = mutableListOf<AudioItem>()

    // Maestro para filtrar/ordenar
    private val audioMaster = mutableListOf<AudioItem>()

    private lateinit var fabOrganize: Button
    private val selected = mutableSetOf<android.net.Uri>()

    // Controles de búsqueda/orden
    private lateinit var etSearch: EditText
    private lateinit var spinnerSort: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_gallery)

        etSearch = findViewById(R.id.etSearchAudios)
        spinnerSort = findViewById(R.id.spinnerSortAudios)

        recyclerView = findViewById(R.id.audioRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fabOrganize = findViewById(R.id.fabOrganizeAudio)
        fabOrganize.setOnClickListener { openOrganizerWithSelected() }
        updateUiTitleFab()

        adapter = AudioAdapter(
            audios = audioList,
            selected = selected,
            onPlay = { item ->
                // abrir player SOLO si no hay selección activa
                if (selected.isEmpty()) {
                    val i = Intent(this, AudioPlayerActivity::class.java).apply {
                        putExtra("uri", item.uri.toString())
                        putExtra("name", item.name)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(i)
                } else {
                    // si hay selección, alterna selección
                    toggleSelect(item.uri)
                }
            },
            onToggleSelect = {
                updateUiTitleFab()
            }
        )
        recyclerView.adapter = adapter

        // Sort options
        val sortOptions = arrayOf("Fecha ↓", "Fecha ↑", "Nombre A-Z", "Nombre Z-A", "Duración ↓", "Duración ↑")
        spinnerSort.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilterAndSort()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilterAndSort() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadAudioFiles()
    }

    override fun onResume() {
        super.onResume()
        loadAudioFiles()
    }

    private fun updateUiTitleFab() {
        val shouldShow = selected.isNotEmpty()
        title = if (shouldShow) "${selected.size} seleccionados" else "Galería de Audios"

        val targetAlpha = if (shouldShow) 1f else 0f
        val targetTrans = if (shouldShow) 0f else 120f

        if (fabOrganize.visibility != View.VISIBLE && shouldShow) {
            fabOrganize.alpha = 0f
            fabOrganize.translationY = 120f
            fabOrganize.visibility = View.VISIBLE
        }

        fabOrganize.animate()
            .alpha(targetAlpha)
            .translationY(targetTrans)
            .setDuration(180)
            .withEndAction {
                if (!shouldShow) fabOrganize.visibility = View.GONE
            }
            .start()
    }


    private fun toggleSelect(uri: android.net.Uri) {
        if (selected.contains(uri)) selected.remove(uri) else selected.add(uri)
        adapter.notifyDataSetChanged()
        updateUiTitleFab()
    }

    private fun openOrganizerWithSelected() {
        val urisTxt = ArrayList(selected.map { it.toString() })
        val intent = Intent(this, OrganizerActivity::class.java).apply {
            putStringArrayListExtra("uris", urisTxt)
        }
        startActivity(intent)
    }

    private fun loadAudioFiles() {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION
        )

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            audioMaster.clear()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "Grabación"
                val date = cursor.getLong(dateColumn)
                val durationMs = cursor.getLong(durationColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                audioMaster.add(AudioItem(name, uri, date, durationMs))
            }
        }

        applyFilterAndSort()
    }

    private fun applyFilterAndSort() {
        val q = etSearch.text?.toString()?.trim()?.lowercase() ?: ""

        val filtered = if (q.isEmpty()) {
            audioMaster
        } else {
            audioMaster.filter { it.name.lowercase().contains(q) }
        }

        val sorted = when (spinnerSort.selectedItemPosition) {
            0 -> filtered.sortedByDescending { it.dateAdded } // Fecha ↓
            1 -> filtered.sortedBy { it.dateAdded }           // Fecha ↑
            2 -> filtered.sortedBy { it.name.lowercase() }    // Nombre A-Z
            3 -> filtered.sortedByDescending { it.name.lowercase() } // Nombre Z-A
            4 -> filtered.sortedByDescending { it.durationMs } // Duración ↓
            else -> filtered.sortedBy { it.durationMs }         // Duración ↑
        }

        audioList.clear()
        audioList.addAll(sorted)
        adapter.notifyDataSetChanged()
        updateUiTitleFab()
    }
}
