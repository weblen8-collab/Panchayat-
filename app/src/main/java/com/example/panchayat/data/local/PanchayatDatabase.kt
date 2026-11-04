package com.example.panchayat.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.panchayat.data.local.dao.NotificationDao
import com.example.panchayat.data.local.dao.PostDao
import com.example.panchayat.data.local.dao.UserDao
import com.example.panchayat.data.local.entity.NotificationEntity
import com.example.panchayat.data.local.entity.PostEntity
import com.example.panchayat.data.local.entity.ReplyEntity
import com.example.panchayat.data.local.entity.UserProfileEntity

@Database(
    entities = [
        PostEntity::class,
        ReplyEntity::class,
        UserProfileEntity::class,
        NotificationEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PanchayatDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun userDao(): UserDao
    abstract fun notificationDao(): NotificationDao

    fun userProfileDao(): UserDao = userDao()

    companion object {
        @Volatile
        private var INSTANCE: PanchayatDatabase? = null

        fun getDatabase(context: Context): PanchayatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PanchayatDatabase::class.java,
                    "panchayat_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
