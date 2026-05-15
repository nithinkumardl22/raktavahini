package com.raktavahini.data.database

import android.content.Context
import androidx.room.*
import com.raktavahini.data.dao.RaktaVahiniDao
import com.raktavahini.data.entities.DonationLogEntity
import com.raktavahini.data.entities.DonorEntity

@Database(
    entities = [DonorEntity::class, DonationLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RaktaVahiniDatabase : RoomDatabase() {
    abstract fun dao(): RaktaVahiniDao

    companion object {
        @Volatile private var INSTANCE: RaktaVahiniDatabase? = null

        fun getInstance(context: Context): RaktaVahiniDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RaktaVahiniDatabase::class.java,
                    "rakta_vahini.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
