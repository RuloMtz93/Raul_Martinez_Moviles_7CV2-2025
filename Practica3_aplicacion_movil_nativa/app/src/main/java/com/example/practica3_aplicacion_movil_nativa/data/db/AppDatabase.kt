package com.example.practica3_aplicacion_movil_nativa.data.db

import android.content.Context
import androidx.room.*
import com.example.practica3_aplicacion_movil_nativa.data.db.dao.MediaDao
import com.example.practica3_aplicacion_movil_nativa.data.db.entities.*

@Database(
    entities = [
        MediaItem::class,
        Album::class,
        Tag::class,
        MediaAlbumCrossRef::class,
        MediaTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "media_library.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
