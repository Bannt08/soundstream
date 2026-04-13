package com.example.soundstream_app.data

import com.example.soundstream_app.R
import com.example.soundstream_app.model.Artist
import com.example.soundstream_app.model.Playlist
import com.example.soundstream_app.model.Song

object MockDataProvider {

    val madeForYouPlaylists = listOf(
        Playlist(
            id = "p1",
            title = "Indonesian pops",
            description = "Nadine amizah, Ghea Indrawari, Yura Yunita",
            imageResId = R.drawable.demo_cover_1
        ),
        Playlist(
            id = "p2",
            title = "90s hiphop mix",
            description = "Classic old school vibe",
            imageResId = R.drawable.demo_cover_2
        ),
        Playlist(
            id = "p3",
            title = "Night chill",
            description = "Lo-fi tunes for focus",
            imageResId = R.drawable.demo_cover_3
        )
    )

    val popularSingers = listOf(
        Artist("a1", "James adam", R.drawable.demo_artist_1),
        Artist("a2", "Nugroho alis", R.drawable.demo_artist_2),
        Artist("a3", "Maria Sriniani", R.drawable.demo_artist_3),
        Artist("a4", "Julian west", R.drawable.demo_artist_4)
    )

    val favoriteSongs = listOf(
        Song(
            id = "s1",
            title = "Hidupku indah",
            artistName = "James adam",
            imageResId = R.drawable.demo_cover_2,
            duration = "3:20"
        ),
        Song(
            id = "s2",
            title = "Begitu adanya",
            artistName = "Nugroho alis",
            imageResId = R.drawable.demo_cover_3,
            duration = "2:58"
        ),
        Song(
            id = "s3",
            title = "Hidup seperti này",
            artistName = "James adam",
            imageResId = R.drawable.demo_cover_1,
            duration = "3:40"
        )
    )

    val nowPlaying = Song(
        id = "np1",
        title = "Hidup như này",
        artistName = "James adam",
        imageResId = R.drawable.demo_cover_1,
        duration = "3:40"
    )

    val playbackQueue = listOf(nowPlaying) + favoriteSongs
}
