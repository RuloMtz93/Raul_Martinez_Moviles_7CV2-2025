package com.example.practica3_aplicacion_movil_nativa

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(
    private val photos: List<Uri>,
    private val listener: OnPhotoClickListener,
    private val selected: MutableSet<Uri> // NUEVO: referencias seleccionadas
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    interface OnPhotoClickListener {
        fun onPhotoClick(photoUri: Uri)
        fun onPhotoLongClick(photoUri: Uri): Boolean
        fun onToggleSelect(photoUri: Uri) {}
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgThumb: ImageView = itemView.findViewById(R.id.imgThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val uri = photos[position]
        Glide.with(holder.itemView.context).load(uri).centerCrop().into(holder.imgThumb)

        // Estado visual de selección
        holder.itemView.alpha = if (selected.contains(uri)) 0.5f else 1f

        holder.itemView.setOnClickListener {
            if (selected.isNotEmpty()) {
                toggle(uri); listener.onToggleSelect(uri)
            } else {
                listener.onPhotoClick(uri)
            }
        }
        holder.itemView.setOnLongClickListener {
            val handled = listener.onPhotoLongClick(uri)
            if (handled) {
                toggle(uri); listener.onToggleSelect(uri)
            }
            handled
        }
    }

    private fun toggle(uri: Uri) {
        if (selected.contains(uri)) selected.remove(uri) else selected.add(uri)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = photos.size
}
