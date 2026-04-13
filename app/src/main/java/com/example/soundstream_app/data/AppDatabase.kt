package com.example.soundstream_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.soundstream_app.R
import com.example.soundstream_app.model.FavoriteSongEntity
import com.example.soundstream_app.model.PlayHistoryEntity
import com.example.soundstream_app.model.PlaylistEntity
import com.example.soundstream_app.model.PlaylistSongCrossRef
import com.example.soundstream_app.model.SongEntity
import com.example.soundstream_app.model.UserEntity
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking

@Database(
    entities = [UserEntity::class, SongEntity::class, FavoriteSongEntity::class, PlaylistEntity::class, PlaylistSongCrossRef::class, PlayHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun songDao(): SongDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playHistoryDao(): PlayHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun populateInitialData(database: AppDatabase) {
            runBlocking {
                val userDao = database.userDao()
                userDao.insertUser(
                    UserEntity(
                        username = "admin",
                        password = "admin123",
                        displayName = "Admin",
                        isPremium = true,
                        isArtist = false,
                        isAdmin = true,
                        token = "token_admin_123"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "premium_user1",
                        password = "premium1",
                        displayName = "Premium One",
                        isPremium = true,
                        isArtist = false,
                        token = "token_premium1"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "premium_user2",
                        password = "premium2",
                        displayName = "Premium Two",
                        isPremium = true,
                        isArtist = false,
                        token = "token_premium2"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "regular_user1",
                        password = "regular1",
                        displayName = "Regular One",
                        isPremium = false,
                        isArtist = false,
                        token = "token_regular1"
                    )
                )
                userDao.insertUser(
                    UserEntity(
                        username = "regular_user2",
                        password = "regular2",
                        displayName = "Regular Two",
                        isPremium = false,
                        isArtist = false,
                        token = "token_regular2"
                    )
                )

                val songDao = database.songDao()
                songDao.insertSong(
                    SongEntity(
                        id = "s1",
                        title = "Hidupku indah",
                        artistName = "James Adam",
                        imageResId = R.drawable.demo_cover_2,
                        duration = "3:20",
                        isUploaded = false,
                        ownerUsername = null
                    )
                )
                songDao.insertSong(
                    SongEntity(
                        id = "s2",
                        title = "Begitu adanya",
                        artistName = "Nugroho Alis",
                        imageResId = R.drawable.demo_cover_3,
                        duration = "2:58",
                        isUploaded = false,
                        ownerUsername = null
                    )
                )
                songDao.insertSong(
                    SongEntity(
                        id = "s3",
                        title = "Hidup seperti ini",
                        artistName = "Maria Sriniani",
                        imageResId = R.drawable.demo_cover_1,
                        duration = "3:40",
                        isUploaded = false,
                        ownerUsername = null
                    )
                )
                songDao.insertSong(
                    SongEntity(
                        id = "u1",
                        title = "Premium Beat",
                        artistName = "Premium One",
                        imageResId = R.drawable.demo_cover_1,
                        duration = "3:10",
                        isUploaded = true,
                        ownerUsername = "premium_user1"
                    )
                )
                songDao.insertSong(
                    SongEntity(
                        id = "u2",
                        title = "VIP Groove",
                        artistName = "Premium Two",
                        imageResId = R.drawable.demo_cover_2,
                        duration = "4:00",
                        isUploaded = true,
                        ownerUsername = "premium_user2"
                    )
                )

                val favoriteDao = database.favoriteDao()
                favoriteDao.addFavorite(FavoriteSongEntity(username = "regular_user1", songId = "s1"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "regular_user1", songId = "u1"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "regular_user1", songId = "s3"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "regular_user2", songId = "s2"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "regular_user2", songId = "u2"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "regular_user2", songId = "s1"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "premium_user1", songId = "s3"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "premium_user1", songId = "u2"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "premium_user2", songId = "s1"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "premium_user2", songId = "u1"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "admin", songId = "u1"))
                favoriteDao.addFavorite(FavoriteSongEntity(username = "admin", songId = "u2"))

                val playlistDao = database.playlistDao()
                playlistDao.insertPlaylist(
                    PlaylistEntity(
                        id = "p1",
                        title = "Chill Vibes",
                        description = "Easy listening for study and unwind.",
                        imageResId = R.drawable.demo_cover_1,
                        ownerUsername = "premium_user1"
                    )
                )
                playlistDao.insertPlaylist(
                    PlaylistEntity(
                        id = "p2",
                        title = "Top Hits",
                        description = "Popular favorites from our curated selection.",
                        imageResId = R.drawable.demo_cover_2,
                        ownerUsername = "premium_user2"
                    )
                )
                playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = "p1", songId = "s1"))
                playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = "p1", songId = "u1"))
                playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = "p1", songId = "s3"))
                playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = "p2", songId = "s2"))
                playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId = "p2", songId = "u2"))

                val historyDao = database.playHistoryDao()
                historyDao.insertPlayHistory(
                    PlayHistoryEntity(
                        username = "regular_user1",
                        songId = "s3",
                        playedAt = System.currentTimeMillis() - 3_600_000
                    )
                )
                historyDao.insertPlayHistory(
                    PlayHistoryEntity(
                        username = "regular_user1",
                        songId = "u1",
                        playedAt = System.currentTimeMillis() - 1_800_000
                    )
                )
                historyDao.insertPlayHistory(
                    PlayHistoryEntity(
                        username = "premium_user2",
                        songId = "s1",
                        playedAt = System.currentTimeMillis() - 2_400_000
                    )
                )
                historyDao.insertPlayHistory(
                    PlayHistoryEntity(
                        username = "admin",
                        songId = "u2",
                        playedAt = System.currentTimeMillis() - 600_000
                    )
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                lateinit var instance: AppDatabase
                val roomCallback = object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Executors.newSingleThreadExecutor().execute {
                            populateInitialData(instance)
                        }
                    }
                }

                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soundstream_app.db"
                ).fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()
                INSTANCE = instance

                Executors.newSingleThreadExecutor().execute {
                    runBlocking {
                        if (instance.userDao().getUser("admin") == null) {
                            populateInitialData(instance)
                        }
                    }
                }

                instance
            }
        }
    }
}
