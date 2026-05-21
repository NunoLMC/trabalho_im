package com.example.trabalho_im.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// --- Ficha 9 | Codelab 4 ---
// Classe da base de dados Room com padrão Singleton
// O Singleton garante que só existe uma instância da base de dados na app
@Database(entities = [Objeto::class], version = 1, exportSchema = false)
abstract class ObjetoDatabase : RoomDatabase() {

    abstract fun objetoDao(): ObjetoDao

    companion object {
        @Volatile
        private var Instance: ObjetoDatabase? = null

        fun getDatabase(context: Context): ObjetoDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ObjetoDatabase::class.java, "objeto_database")
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
