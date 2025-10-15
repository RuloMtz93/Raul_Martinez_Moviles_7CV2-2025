package com.example.practica3_aplicacion_movil_nativa

import android.os.Bundle
import android.widget.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.practica3_aplicacion_movil_nativa.data.db.AppDatabase
import com.example.practica3_aplicacion_movil_nativa.data.db.dao.MediaDao
import com.example.practica3_aplicacion_movil_nativa.data.db.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrganizerActivity : BaseActivity() {

    private lateinit var dao: MediaDao
    private lateinit var rvAlbums: RecyclerView
    private lateinit var rvTags: RecyclerView
    private lateinit var btnNewAlbum: Button
    private lateinit var btnNewTag: Button
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button
    private lateinit var tvHint: TextView

    private val selectedUris = mutableListOf<String>() // llegadas por intent
    private val albums = mutableListOf<Album>()
    private val tags = mutableListOf<Tag>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organizer)

        dao = AppDatabase.get(this).mediaDao()

        rvAlbums = findViewById(R.id.rvAlbums)
        rvTags = findViewById(R.id.rvTags)
        btnNewAlbum = findViewById(R.id.btnNewAlbum)
        btnNewTag = findViewById(R.id.btnNewTag)
        etSearch = findViewById(R.id.etSearch)
        btnSearch = findViewById(R.id.btnSearch)
        tvHint = findViewById(R.id.tvHint)

        rvAlbums.layoutManager = LinearLayoutManager(this)
        rvTags.layoutManager = LinearLayoutManager(this)

        val urisIn = intent.getStringArrayListExtra("uris") ?: arrayListOf()
        selectedUris.addAll(urisIn)
        tvHint.text = "Elementos seleccionados: ${selectedUris.size}"

        rvAlbums.adapter = SimpleTextAdapter(
            data = albums.map { it.name }.toMutableList(),
            onClick = { idx -> assignToAlbum(albums[idx]) }
        )
        rvTags.adapter = SimpleTextAdapter(
            data = tags.map { it.name }.toMutableList(),
            onClick = { idx -> assignTag(tags[idx]) }
        )

        btnNewAlbum.setOnClickListener { promptNewAlbum() }
        btnNewTag.setOnClickListener { promptNewTag() }
        btnSearch.setOnClickListener { runSearch() }

        refreshLists()
    }

    private fun refreshLists() {
        lifecycleScope.launch {
            val (al, tg) = withContext(Dispatchers.IO) {
                dao.getAlbums() to dao.getTags()
            }
            albums.clear(); albums.addAll(al)
            tags.clear(); tags.addAll(tg)

            (rvAlbums.adapter as SimpleTextAdapter).setData(albums.map { it.name })
            (rvTags.adapter as SimpleTextAdapter).setData(tags.map { it.name })
        }
    }

    private fun promptNewAlbum() {
        val input = EditText(this)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nuevo álbum")
            .setView(input)
            .setPositiveButton("Crear") { d, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) createAlbum(name)
                d.dismiss()
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun promptNewTag() {
        val input = EditText(this)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nueva etiqueta")
            .setView(input)
            .setPositiveButton("Crear") { d, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) createTag(name)
                d.dismiss()
            }.setNegativeButton("Cancelar", null).show()
    }

    private fun createAlbum(name: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            dao.insertAlbum(Album(name = name))
            withContext(Dispatchers.Main) { refreshLists() }
        }
    }

    private fun createTag(name: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            dao.insertTag(Tag(name = name))
            withContext(Dispatchers.Main) { refreshLists() }
        }
    }

    private fun assignToAlbum(album: Album) {
        lifecycleScope.launch(Dispatchers.IO) {
            val mediaIds = ensureMediaItems(selectedUris)
            mediaIds.forEach { id ->
                dao.insertMediaAlbumCrossRef(MediaAlbumCrossRef(mediaId = id, albumId = album.id))
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@OrganizerActivity, "Asignado a álbum \"${album.name}\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun assignTag(tag: Tag) {
        lifecycleScope.launch(Dispatchers.IO) {
            val mediaIds = ensureMediaItems(selectedUris)
            mediaIds.forEach { id ->
                dao.insertMediaTagCrossRef(MediaTagCrossRef(mediaId = id, tagId = tag.id))
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@OrganizerActivity, "Etiqueta \"${tag.name}\" aplicada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun ensureMediaItems(uris: List<String>): List<Long> {
        val ids = mutableListOf<Long>()
        for (u in uris) {
            val existing = dao.getMediaByUri(u)
            if (existing != null) {
                ids += existing.id
            } else {
                // Tipo heurístico por extensión
                val type = if (u.endsWith(".jpg", true) || u.endsWith(".jpeg", true) || u.startsWith("content://media/external/images")) "photo" else "audio"
                val insertedId = dao.insertMedia(
                    MediaItem(uri = u, type = type, displayName = null, dateAdded = null, durationMs = null)
                )
                if (insertedId > 0) ids += insertedId else dao.getMediaByUri(u)?.let { ids += it.id }
            }
        }
        return ids
    }

    private fun runSearch() {
        val q = etSearch.text.toString().trim()
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                dao.searchMedia(type = null, q = if (q.isEmpty()) null else q, order = "name")
            }
            val names = result.map { it.displayName ?: it.uri.substringAfterLast('/') }
            androidx.appcompat.app.AlertDialog.Builder(this@OrganizerActivity)
                .setTitle("Resultado búsqueda (${result.size})")
                .setItems(names.toTypedArray(), null)
                .setPositiveButton("Aceptar", null)
                .show()
        }
    }

    // Adaptador simple de Strings
    class SimpleTextAdapter(
        private var data: MutableList<String>,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.Adapter<SimpleTextAdapter.VH>() {

        fun setData(newData: List<String>) {
            data.clear(); data.addAll(newData); notifyDataSetChanged()
        }

        class VH(v: android.view.View) : RecyclerView.ViewHolder(v) {
            val tv: TextView = v.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.tv.text = data[position]
            holder.itemView.setOnClickListener { onClick(position) }
        }

        override fun getItemCount(): Int = data.size
    }
}
