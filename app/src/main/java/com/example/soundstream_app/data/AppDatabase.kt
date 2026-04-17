package com.example.soundstream_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.soundstream_app.R
import com.example.soundstream_app.data.RawAudioProvider
import com.example.soundstream_app.model.FavoriteSongEntity
import com.example.soundstream_app.model.PlaylistEntity
import com.example.soundstream_app.model.PlaylistSongCrossRef
import com.example.soundstream_app.model.SongEntity
import com.example.soundstream_app.model.UserEntity
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
        PlaylistSongCrossRef::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun songDao(): SongDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val initializationComplete = CompletableDeferred<Unit>()

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS users_new (
                        username TEXT NOT NULL PRIMARY KEY,
                        passwordHash TEXT NOT NULL,
                        passwordSalt TEXT NOT NULL,
                        displayName TEXT,
                        isPremium INTEGER NOT NULL DEFAULT 0,
                        isArtist INTEGER NOT NULL DEFAULT 0,
                        isAdmin INTEGER NOT NULL DEFAULT 0,
                        token TEXT
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO users_new (username, passwordHash, passwordSalt, displayName, isPremium, isArtist, isAdmin, token)
                    SELECT username, passwordHash, passwordSalt, displayName, isPremium, isArtist, isAdmin, token FROM users
                    """.trimIndent()
                )
                database.execSQL("DROP TABLE IF EXISTS users")
                database.execSQL("ALTER TABLE users_new RENAME TO users")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS play_history")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN avatarResId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATIONS = arrayOf(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)

        private suspend fun populateInitialData(context: Context, database: AppDatabase) {
            val songDao = database.songDao()
            if (songDao.getAllSongs().isEmpty()) {
                RawAudioProvider.getSongEntities(context).forEach { songDao.insertSong(it) }
            }

            val userDao = database.userDao()
            if (userDao.countUsers() == 0) {
                installSeedUsers(userDao)
            }
        }

        private suspend fun repairInitialData(context: Context, database: AppDatabase) {
            val songDao = database.songDao()
            RawAudioProvider.getSongEntities(context).forEach { songDao.insertSong(it) }

            val userDao = database.userDao()
            installSeedUsers(userDao)
        }

        private suspend fun installSeedUsers(userDao: UserDao) {
            listOf(
                Triple("thang", "thang", "Thang"),
                Triple("test", "test", "Test")
            ).forEach { (username, password, displayName) ->
                val salt = PasswordUtils.generateSalt()
                val hash = PasswordUtils.hashPassword(password, salt)
                userDao.insertUser(
                    UserEntity(
                        username = username,
                        passwordHash = hash,
                        passwordSalt = salt,
                        displayName = displayName,
                        isPremium = true,
                        isArtist = true,
                        isAdmin = false,
                        token = UUID.randomUUID().toString(),
                        avatarResId = R.drawable.uth
                    )
                )
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
                    .also { instance ->
                        INSTANCE = instance
                    }
            }
        }

        fun initialize(context: Context, scope: CoroutineScope) {
            val instance = getInstance(context)
            scope.launch {
                try {
                    val songsEmpty = instance.songDao().getAllSongs().isEmpty()
                    val usersEmpty = instance.userDao().countUsers() == 0
                    if (songsEmpty || usersEmpty) {
                        populateInitialData(context, instance)
                    } else {
                        repairInitialData(context, instance)
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    if (!initializationComplete.isCompleted) {
                        initializationComplete.complete(Unit)
                    }
                }
            }
        }

        suspend fun waitForInitialization(context: Context) {
            getInstance(context)
            initializationComplete.await()
        }
    }
}
