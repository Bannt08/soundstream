package com.example.soundstream_app.data

import android.content.Context
import android.media.MediaMetadataRetriever
import com.example.soundstream_app.R
import com.example.soundstream_app.model.Artist
import com.example.soundstream_app.model.Playlist
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.SongEntity

object RawAudioProvider {

    private val rawSongMetadata = listOf(
        RawAudioEntry("dai_lo_mat_troi", "Dai lo mat troi", "Unknown artist", R.drawable.uth),
        RawAudioEntry("day_la_uth", "Day la UTH", "UTH", R.drawable.uth),
        RawAudioEntry("di_dau_de_thay_hoa_bay", "Di dau de thay hoa bay", "Unknown artist", R.drawable.uth),
        RawAudioEntry("em_khong", "Em khong", "Unknown artist", R.drawable.uth),
        RawAudioEntry("ly_giai", "Ly giai", "Unknown artist", R.drawable.uth),
        RawAudioEntry("mot_thoi", "Mot thoi", "Unknown artist", R.drawable.uth),
        RawAudioEntry("nua_thap_ky", "Nua thap ky", "Unknown artist", R.drawable.uth),
        RawAudioEntry("no_hoa", "No hoa", "Unknown artist", R.drawable.uth),
        RawAudioEntry("only", "Only", "Unknown artist", R.drawable.uth),
        RawAudioEntry("uth", "Uth", "Unknown artist", R.drawable.uth)
    )

    private val playlistCoverResIds = listOf(
        R.drawable.img1,
        R.drawable.img2
    )

    val madeForYouPlaylists = listOf(
        Playlist(
            id = "p1",
            title = "Tuyển tập nhạc",
            description = "Các bài hát đang hot",
            imageResId = R.drawable.img1
        ),
        Playlist(
            id = "p2",
            title = "Ưa thích",
            description = "Nhạc yêu thích của bạn",
            imageResId = R.drawable.img2
        )
    )

    val popularSingers = listOf(
        Artist("a1", "Unknown Artist", R.drawable.demo_artist_1),
        Artist("a2", "Various Artists", R.drawable.demo_artist_2)
    )

    fun getRandomMadeForYouPlaylists(songs: List<Song>): List<Playlist> {
        if (songs.isEmpty()) return madeForYouPlaylists

        val shuffledSongs = songs.shuffled()
        val playlistTitles = listOf("Hôm nay nghe gì", "Giai điệu ngẫu nhiên", "Tuyển tập bất chợt")
        val playlistDescriptions = listOf(
            "Chọn lọc từ các bài hát đang phát",
            "Album được tạo tự động từ danh sách nhạc",
            "Nghe thử những giai điệu mới lạ"
        )

        return playlistTitles.mapIndexed { index, title ->
            Playlist(
                id = "random_album_$index",
                title = title,
                description = playlistDescriptions.getOrNull(index) ?: "Album chơi nhạc ngẫu nhiên",
                imageResId = playlistCoverResIds[index % playlistCoverResIds.size]
            )
        }
    }

    fun getSongs(context: Context): List<Song> {
        return rawSongMetadata.mapNotNull { entry ->
            val rawResId = context.resources.getIdentifier(entry.resourceName, "raw", context.packageName)
            if (rawResId == 0) return@mapNotNull null
            Song(
                id = entry.resourceName,
                title = entry.title,
                artistName = entry.artist,
                imageResId = entry.imageResId,
                duration = getFormattedDuration(context, rawResId),
                rawResId = rawResId
            )
        }
    }

    fun getSongEntities(context: Context): List<SongEntity> {
        return getSongs(context).map { song ->
            SongEntity(
                id = song.id,
                title = song.title,
                artistName = song.artistName,
                imageResId = song.imageResId,
                duration = song.duration,
                rawResId = song.rawResId,
                isUploaded = false,
                ownerUsername = null
            )
        }
    }

    private fun getFormattedDuration(context: Context, rawResId: Int): String {
        return try {
            val retriever = MediaMetadataRetriever()
            val afd = context.resources.openRawResourceFd(rawResId)
            afd.use {
                retriever.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val minutes = duration / 1000 / 60
            val seconds = (duration / 1000 % 60)
            String.format("%d:%02d", minutes, seconds)
        } catch (ex: Exception) {
            "0:00"
        }
    }

    private data class RawAudioEntry(
        val resourceName: String,
        val title: String,
        val artist: String,
        val imageResId: Int
    )
}
