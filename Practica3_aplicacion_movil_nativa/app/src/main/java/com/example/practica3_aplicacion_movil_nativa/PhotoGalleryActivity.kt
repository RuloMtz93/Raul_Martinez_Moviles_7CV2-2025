package com.example.practica3_aplicacion_movil_nativa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PhotoGalleryActivity : BaseActivity(), PhotoAdapter.OnPhotoClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PhotoAdapter

    // Lista visible que consume el adapter (URIs)
    private val photoList = mutableListOf<Uri>()

    // Maestro con metadatos para filtrar/ordenar
    private data class PhotoMeta(val name: String, val uri: Uri, val dateAdded: Long)
    private val photoMaster = mutableListOf<PhotoMeta>()

    private lateinit var fabOrganize: Button
    private val selected = mutableSetOf<Uri>()

    // Controles de búsqueda/orden
    private lateinit var etSearch: EditText
    private lateinit var spinnerSort: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_gallery)

        etSearch = findViewById(R.id.etSearchPhotos)
        spinnerSort = findViewById(R.id.spinnerSortPhotos)

        recyclerView = findViewById(R.id.recyclerPhotos)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = PhotoAdapter(photoList, this, selected)
        recyclerView.adapter = adapter

        fabOrganize = findViewById(R.id.fabOrganize)
        fabOrganize.setOnClickListener { openOrganizerWithSelected() }
        updateFab()

        // Sort options
        val sortOptions = arrayOf("Fecha ↓", "Fecha ↑", "Nombre A-Z", "Nombre Z-A")
        spinnerSort.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                applyFilterAndSort()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { applyFilterAndSort() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        loadPhotos()
    }

    private fun updateFab() {
        val shouldShow = selected.isNotEmpty()
        title = if (shouldShow) "${selected.size} seleccionadas" else "Galería de Fotos"

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


    private fun openOrganizerWithSelected() {
        val urisTxt = ArrayList(selected.map { it.toString() })
        val intent = Intent(this, OrganizerActivity::class.java).apply {
            putStringArrayListExtra("uris", urisTxt)
        }
        startActivity(intent)
    }

    private fun loadPhotos() {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            photoMaster.clear()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: "IMG_$id"
                val date = cursor.getLong(dateColumn)
                val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                photoMaster.add(PhotoMeta(name, uri, date))
            }
        }

        applyFilterAndSort()
        Log.d("PhotoGallery", "Fotos cargadas: ${photoList.size}")
    }

    private fun applyFilterAndSort() {
        val q = etSearch.text?.toString()?.trim()?.lowercase() ?: ""

        // 1) Filtrar por nombre
        val filtered = if (q.isEmpty()) {
            photoMaster
        } else {
            photoMaster.filter { it.name.lowercase().contains(q) }
        }

        // 2) Ordenar
        val sorted = when (spinnerSort.selectedItemPosition) {
            0 -> filtered.sortedByDescending { it.dateAdded } // Fecha ↓
            1 -> filtered.sortedBy { it.dateAdded }           // Fecha ↑
            2 -> filtered.sortedBy { it.name.lowercase() }    // Nombre A-Z
            else -> filtered.sortedByDescending { it.name.lowercase() } // Nombre Z-A
        }

        // 3) Reconstruir lista visible de URIs
        photoList.clear()
        photoList.addAll(sorted.map { it.uri })
        adapter.notifyDataSetChanged()

        // Mantiene selección y FAB (no la limpia para que puedas organizar aunque filtres)
        updateFab()
    }

    // --- Callbacks adapter ---
    override fun onPhotoClick(photoUri: Uri) {
        if (selected.isNotEmpty()) {
            if (selected.contains(photoUri)) selected.remove(photoUri) else selected.add(photoUri)
            adapter.notifyDataSetChanged()
            updateFab()
            return
        }
        val intent = Intent(this, PhotoDetailActivity::class.java)
        intent.putExtra("photoUri", photoUri.toString())
        startActivity(intent)
    }

    override fun onPhotoLongClick(photoUri: Uri): Boolean {
        // Deja que el adapter haga el toggle cuando devolvemos true
        return true
    }

    override fun onToggleSelect(photoUri: Uri) {
        updateFab()
    }
}
