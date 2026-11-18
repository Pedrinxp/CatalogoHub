package com.example.catalogohub.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Banco de dados local (Room).
 * Versão 2: adicionou a tabela de pedidos. Como é app de teste, o banco antigo é recriado
 * (desinstale o app do emulador antes de rodar esta versão).
 */
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
                    // Versão atual (não depreciada) do fallback destrutivo
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}