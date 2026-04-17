package com.example.soundstream_app.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.example.soundstream_app.model.Song

object PlaybackManager {
    private var applicationContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private val playbackStateChangedListeners = mutableListOf<() -> Unit>()
    private val progressHandler = Handler(Looper.getMainLooper())
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

    val currentSong: Song?
        get() = playbackQueue.getOrNull(currentIndex)

    val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    val currentPosition: Int
        get() = mediaPlayer?.currentPosition ?: 0

    val duration: Int
        get() = mediaPlayer?.duration ?: 0

    val progressPercent: Int
        get() = if (duration > 0) (currentPosition * 100 / duration) else 0

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
        val currentSong = currentSong ?: return

        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = createMediaPlayer(context, currentSong)

        mediaPlayer?.let { player ->
            player.isLooping = false
            player.setOnCompletionListener {
                playNext()
            }
            player.start()
        }

        notifyPlaybackStateChanged()
        scheduleProgressUpdate()
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
        val nextIndex = if (playbackQueue.isEmpty()) return else (currentIndex + 1).coerceAtMost(playbackQueue.lastIndex)
        if (nextIndex == currentIndex) return
        play(playbackQueue[nextIndex], playbackQueue)
    }

    fun playPrevious() {
        if (playbackQueue.isEmpty()) return
        val prevIndex = if (currentIndex - 1 < 0) 0 else currentIndex - 1
        play(playbackQueue[prevIndex], playbackQueue)
    }

    private fun createMediaPlayer(context: Context, song: Song): MediaPlayer? {
        return try {
            val player = when {
                song.rawResId != null -> MediaPlayer.create(context, song.rawResId)
                !song.sourceUri.isNullOrBlank() -> MediaPlayer.create(context, Uri.parse(song.sourceUri))
                else -> null
            }
            player
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}
