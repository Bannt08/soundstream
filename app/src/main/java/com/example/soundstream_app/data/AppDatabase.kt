package com.example.soundstream_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.soundstream_app.R
import com.example.soundstream_app.model.*
import com.example.soundstream_app.util.PasswordUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        UserEntity::class,
        SongEntity::class,
        FavoriteSongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlayHistoryEntity::class
    ],
    version = 9,
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
        private val initializationComplete = CompletableDeferred<Unit>()

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS users_new (username TEXT NOT NULL PRIMARY KEY, passwordHash TEXT NOT NULL, passwordSalt TEXT NOT NULL, displayName TEXT, isPremium INTEGER NOT NULL DEFAULT 0, isArtist INTEGER NOT NULL DEFAULT 0, isAdmin INTEGER NOT NULL DEFAULT 0, token TEXT)")
                db.execSQL("INSERT OR IGNORE INTO users_new (username, passwordHash, passwordSalt, displayName, isPremium, isArtist, isAdmin, token) SELECT username, passwordHash, passwordSalt, displayName, isPremium, isArtist, isAdmin, token FROM users")
                db.execSQL("DROP TABLE IF EXISTS users")
                db.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS play_history")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN avatarResId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS play_history (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, username TEXT NOT NULL, songId TEXT NOT NULL, timestamp INTEGER NOT NULL)")
            }
        }

        private val MIGRATIONS = arrayOf(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)

        private suspend fun installSeedUsers(userDao: UserDao) {
            val seeds = listOf(
                Triple("thang", "thang", "Thang Admin"),
                Triple("giang204", "123456", "Nguyễn Trường Giang"),
                Triple("ngoc", "123456", "ngoc test"),
                Triple("test", "test", "Test User")
            )
            seeds.forEach { (u, p, n) ->
                val salt = PasswordUtils.generateSalt()
                userDao.insertUser(UserEntity(
                    username = u, passwordHash = PasswordUtils.hashPassword(p, salt),
                    passwordSalt = salt, displayName = n,
                    isPremium = u == "thang", isArtist = u == "giang204" || u == "thanhhau",
                    isAdmin = u == "thang", token = UUID.randomUUID().toString(),
                    avatarResId = R.drawable.uth
                ))
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soundstream_app.db"
                )
                    .addMigrations(*MIGRATIONS)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }

        fun initialize(context: Context, scope: CoroutineScope) {
            val instance = getInstance(context)
            scope.launch {
                try {
                    if (instance.userDao().countUsers() == 0) {
                        installSeedUsers(instance.userDao())
                    }
                    if (instance.songDao().getAllSongs().isEmpty()) {
                        RawAudioProvider.getSongEntities(context).forEach { instance.songDao().insertSong(it) }
                    }
                } catch (e: Exception) { e.printStackTrace() }
                finally { initializationComplete.complete(Unit) }
            }
        }

        suspend fun waitForInitialization(context: Context) {
            getInstance(context)
            initializationComplete.await()
        }
    }
}