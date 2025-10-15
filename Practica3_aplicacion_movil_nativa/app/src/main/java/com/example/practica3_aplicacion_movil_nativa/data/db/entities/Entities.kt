package com.example.practica3_aplicacion_movil_nativa.data.db.entities

import androidx.room.*

@Entity(
    indices = [Index(value = ["uri"], unique = true)]
)
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String,
    val type: String, // "photo" | "audio"
    val displayName: String? = null,
    val dateAdded: Long? = null,
    val durationMs: Long? = null
)

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Album(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(
    primaryKeys = ["mediaId", "albumId"],
    indices = [Index("albumId"), Index("mediaId")],
    foreignKeys = [
        ForeignKey(entity = MediaItem::class, parentColumns = ["id"], childColumns = ["mediaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Album::class, parentColumns = ["id"], childColumns = ["albumId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class MediaAlbumCrossRef(
    val mediaId: Long,
    val albumId: Long
)

@Entity(
    primaryKeys = ["mediaId", "tagId"],
    indices = [Index("tagId"), Index("mediaId")],
    foreignKeys = [
        ForeignKey(entity = MediaItem::class, parentColumns = ["id"], childColumns = ["mediaId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Tag::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class MediaTagCrossRef(
    val mediaId: Long,
    val tagId: Long
)

data class AlbumWithMedia(
    @Embedded val album: Album,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MediaAlbumCrossRef::class,
            parentColumn = "albumId",
            entityColumn = "mediaId"
        )
    )
    val media: List<MediaItem>
)

data class TagWithMedia(
    @Embedded val tag: Tag,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MediaTagCrossRef::class,
            parentColumn = "tagId",
            entityColumn = "mediaId"
        )
    )
    val media: List<MediaItem>
)
