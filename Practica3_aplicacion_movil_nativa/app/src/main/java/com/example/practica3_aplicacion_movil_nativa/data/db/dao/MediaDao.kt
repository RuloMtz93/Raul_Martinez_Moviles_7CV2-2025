package com.example.practica3_aplicacion_movil_nativa.data.db.dao

import androidx.room.*
import com.example.practica3_aplicacion_movil_nativa.data.db.entities.*

@Dao
interface MediaDao {

    // --- Media ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMedia(item: MediaItem): Long

    @Query("SELECT * FROM MediaItem WHERE uri = :uri LIMIT 1")
    suspend fun getMediaByUri(uri: String): MediaItem?

    @Query("""
        SELECT * FROM MediaItem
        WHERE (:type IS NULL OR type = :type)
          AND (:q IS NULL OR displayName LIKE '%' || :q || '%')
        ORDER BY
          CASE WHEN :order = 'name' THEN displayName END ASC,
          CASE WHEN :order = 'date' THEN dateAdded END DESC,
          CASE WHEN :order = 'duration' THEN durationMs END DESC
    """)
    suspend fun searchMedia(type: String?, q: String?, order: String): List<MediaItem>

    // --- Album ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbum(album: Album): Long

    @Query("SELECT * FROM Album ORDER BY name ASC")
    suspend fun getAlbums(): List<Album>

    @Transaction
    @Query("SELECT * FROM Album WHERE id = :albumId")
    suspend fun getAlbumWithMedia(albumId: Long): List<AlbumWithMedia>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediaAlbumCrossRef(ref: MediaAlbumCrossRef): Long

    // --- Tag ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag): Long

    @Query("SELECT * FROM Tag ORDER BY name ASC")
    suspend fun getTags(): List<Tag>

    @Transaction
    @Query("SELECT * FROM Tag WHERE id = :tagId")
    suspend fun getTagWithMedia(tagId: Long): List<TagWithMedia>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMediaTagCrossRef(ref: MediaTagCrossRef): Long
}
