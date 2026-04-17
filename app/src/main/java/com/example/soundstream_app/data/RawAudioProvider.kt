package com.example.soundstream_app.data

import android.content.Context
import android.media.MediaMetadataRetriever
import com.example.soundstream_app.R
import com.example.soundstream_app.model.Artist
import com.example.soundstream_app.model.Playlist
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.SongEntity

object RawAudioProvider {

    // Danh sách 10 bài hát mẫu khớp với thư mục res/raw
    private val rawSongMetadata = listOf(
        RawAudioEntry("dai_lo_mat_troi", "Đại lộ mặt trời", "UTH Artist", R.drawable.uth),
        RawAudioEntry("day_la_uth", "Đây là UTH", "UTH Artist", R.drawable.uth),
        RawAudioEntry("di_dau_de_thay_hoa_bay", "Đi đâu để thấy hoa bay", "UTH Artist", R.drawable.uth),
        RawAudioEntry("em_khong", "Em không", "UTH Artist", R.drawable.uth),
        RawAudioEntry("ly_giai", "Lý giải", "UTH Artist", R.drawable.uth),
        RawAudioEntry("mot_thoi", "Một thời", "UTH Artist", R.drawable.uth),
        RawAudioEntry("nua_thap_ky", "Nửa thập kỷ", "UTH Artist", R.drawable.uth),
        RawAudioEntry("no_hoa", "Nở hoa", "UTH Artist", R.drawable.uth),
        RawAudioEntry("only", "Only", "UTH Artist", R.drawable.uth),
        RawAudioEntry("uth", "UTH", "UTH Artist", R.drawable.uth)
    )

    private val playlistCoverResIds = listOf(
        R.drawable.img1,
        R.drawable.img2
    )

    // Dữ liệu mẫu cho màn hình Discover (Người 2)
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

    /**
     * Chuyển đổi metadata từ raw thành danh sách Song Entity để lưu vào Room Database (Người 4)
     */
    fun getSongEntities(context: Context): List<SongEntity> {
        return rawSongMetadata.mapNotNull { entry ->
            val rawResId = context.resources.getIdentifier(entry.resourceName, "raw", context.packageName)
            if (rawResId == 0) return@mapNotNull null

            SongEntity(
                id = entry.resourceName,
                title = entry.title,
                artistName = entry.artist,
                imageResId = entry.imageResId,
                duration = getFormattedDuration(context, rawResId),
                rawResId = rawResId,
                isUploaded = false,
                ownerUsername = null // Nhạc hệ thống không có chủ sở hữu
            )
        }
    }

    /**
     * Trả về danh sách Song dùng cho hiển thị UI (Người 2)
     */
    fun getSongs(context: Context): List<Song> {
        return getSongEntities(context).map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artistName = entity.artistName,
                imageResId = entity.imageResId,
                duration = entity.duration,
                rawResId = entity.rawResId
            )
        }
    }

    /**
     * Tự động tính toán độ dài bài hát từ file raw
     */
    private fun getFormattedDuration(context: Context, rawResId: Int): String {
        val retriever = MediaMetadataRetriever()
        return try {
            val afd = context.resources.openRawResourceFd(rawResId)
            retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            afd.close()

            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
            val minutes = (durationMs / 1000) / 60
            val seconds = (durationMs / 1000) % 60
            String.format("%d:%02d", minutes, seconds)
        } catch (e: Exception) {
            "3:00"
        } finally {
            retriever.release()
        }
    }

    // Cấu trúc nội bộ để định nghĩa metadata bài hát
    private data class RawAudioEntry(
        val resourceName: String,
        val title: String,
        val artist: String,
        val imageResId: Int
    )
}