package com.example.soundstream_app.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.net.toUri
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.PlayHistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PlaybackManager {
    private var applicationContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private val playbackStateChangedListeners = mutableListOf<() -> Unit>()
    private val progressHandler = Handler(Looper.getMainLooper())
    private val playbackScope = CoroutineScope(Dispatchers.IO)

    private val progressUpdater = object : Runnable {
        override fun run() {
            notifyPlaybackStateChanged()
            scheduleProgressUpdate()
        }
    }

    var playbackQueue: List<Song> = emptyList()
        private set

    var currentIndex: Int = 0
        private set

    val currentSong: Song? get() = playbackQueue.getOrNull(currentIndex)
    val isPlaying: Boolean get() = mediaPlayer?.isPlaying == true
    val currentPosition: Int get() = mediaPlayer?.currentPosition ?: 0
    val duration: Int get() = mediaPlayer?.duration ?: 0
    val progressPercent: Int get() = if (duration > 0) (currentPosition * 100 / duration) else 0

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun addPlaybackStateChangedListener(listener: () -> Unit) {
        playbackStateChangedListeners.add(listener)
    }

    fun removePlaybackStateChangedListener(listener: () -> Unit) {
        playbackStateChangedListeners.remove(listener)
    }

    private fun notifyPlaybackStateChanged() {
        playbackStateChangedListeners.forEach { it.invoke() }
    }

    private fun scheduleProgressUpdate() {
        progressHandler.removeCallbacks(progressUpdater)
        if (isPlaying) {
            progressHandler.postDelayed(progressUpdater, 500)
        }
    }

    fun play(song: Song, queue: List<Song> = listOf(song)) {
        val context = applicationContext ?: return
        val startIndex = queue.indexOfFirst { it.id == song.id }.takeIf { it >= 0 } ?: 0
        playbackQueue = queue
        currentIndex = startIndex
        val songToPlay = currentSong ?: return

        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = createMediaPlayer(context, songToPlay)

        mediaPlayer?.let { player ->
            player.isLooping = false
            player.setOnCompletionListener { playNext() }
            player.start()
            saveToHistory(context, songToPlay.id)
        }

        notifyPlaybackStateChanged()
        scheduleProgressUpdate()
    }

    private fun saveToHistory(context: Context, songId: String) {
        val user = SessionManager.currentUser ?: return
        if (user.isGuest) return

        playbackScope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                db.playHistoryDao().insertHistory(
                    PlayHistoryEntity(
                        username = user.username,
                        songId = songId,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playQueue(queue: List<Song>, startIndex: Int = 0) {
        if (queue.isEmpty()) return
        play(queue[startIndex.coerceIn(queue.indices)], queue)
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            notifyPlaybackStateChanged()
            progressHandler.removeCallbacks(progressUpdater)
        }
    }

    fun resume() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            notifyPlaybackStateChanged()
            scheduleProgressUpdate()
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        playbackQueue = emptyList()
        currentIndex = 0
        notifyPlaybackStateChanged()
        progressHandler.removeCallbacks(progressUpdater)
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.let {
            val safePosition = positionMs.coerceIn(0, duration)
            it.seekTo(safePosition)
            notifyPlaybackStateChanged()
        }
    }

    fun playNext() {
        if (playbackQueue.isEmpty()) return
        val nextIndex = (currentIndex + 1)
        if (nextIndex <= playbackQueue.lastIndex) {
            play(playbackQueue[nextIndex], playbackQueue)
        }
    }

    fun playPrevious() {
        if (playbackQueue.isEmpty()) return
        val prevIndex = (currentIndex - 1).coerceAtLeast(0)
        play(playbackQueue[prevIndex], playbackQueue)
    }

    private fun createMediaPlayer(context: Context, song: Song): MediaPlayer? {
        return try {
            val resId = song.rawResId ?: 0 // Sửa lỗi Type Mismatch tại đây
            when {
                resId != 0 -> MediaPlayer.create(context, resId)
                !song.sourceUri.isNullOrBlank() -> MediaPlayer.create(context, song.sourceUri.toUri())
                else -> null
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}