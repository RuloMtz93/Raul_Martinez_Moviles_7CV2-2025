package com.example.practica3_aplicacion_movil_nativa

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AudioItem(
    val name: String,
    val uri: Uri,
    val dateAdded: Long,
    val durationMs: Long
)

class AudioAdapter(
    private val audios: List<AudioItem>,
    private val selected: MutableSet<Uri>,
    private val onPlay: (AudioItem) -> Unit,
    private val onToggleSelect: () -> Unit
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {

    class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.audioName)
        val date: TextView = view.findViewById(R.id.audioDate)
        val duration: TextView = view.findViewById(R.id.audioDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_audio, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audio = audios[position]
        holder.name.text = audio.name
        holder.date.text = "Fecha: ${
            java.text.SimpleDateFormat("dd/MM/yyyy").format(audio.dateAdded * 1000)
        }"
        holder.duration.text = formatDuration(audio.durationMs)

        // feedback visual de selección
        holder.itemView.alpha = if (selected.contains(audio.uri)) 0.5f else 1f

        holder.itemView.setOnClickListener {
            if (selected.isNotEmpty()) {
                toggle(audio.uri)
                onToggleSelect()
            } else {
                onPlay(audio)
            }
        }

        holder.itemView.setOnLongClickListener {
            toggle(audio.uri)
            onToggleSelect()
            true
        }
    }

    override fun getItemCount(): Int = audios.size

    private fun toggle(uri: Uri) {
        if (selected.contains(uri)) selected.remove(uri) else selected.add(uri)
        notifyDataSetChanged()
    }

    private fun formatDuration(ms: Long): String {
        if (ms <= 0) return "00:00"
        val totalSec = ms / 1000
        val mm = totalSec / 60
        val ss = totalSec % 60
        return String.format("%02d:%02d", mm, ss)
    }
}
