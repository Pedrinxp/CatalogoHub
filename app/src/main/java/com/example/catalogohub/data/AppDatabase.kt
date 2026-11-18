package com.example.catalogohub.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
@Database(
    entities = [
        ProdutoEntity::class,
        NotaFiscalEntity::class,
        LogSyncEntity::class,
        PedidoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun produtoDao(): ProdutoDao
    abstract fun notaFiscalDao(): NotaFiscalDao
    abstract fun logSyncDao(): LogSyncDao
    abstract fun pedidoDao(): PedidoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "catalogohub.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
