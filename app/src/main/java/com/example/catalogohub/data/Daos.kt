package com.example.catalogohub.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProdutoDao {
    @Query("SELECT * FROM produtos ORDER BY nome COLLATE NOCASE ASC")
    fun observarTodos(): Flow<List<ProdutoEntity>>

    /** Lista única (sem observar), usada para gerar pedidos de exemplo. */
    @Query("SELECT * FROM produtos")
    suspend fun listar(): List<ProdutoEntity>

    @Query("SELECT * FROM produtos WHERE id = :id")
    suspend fun buscarPorId(id: Long): ProdutoEntity?

    @Query("SELECT COUNT(*) FROM produtos")
    suspend fun contar(): Int

    @Query("SELECT COUNT(*) FROM produtos WHERE UPPER(sku) = UPPER(:sku) AND id != :ignorarId")
    suspend fun contarPorSku(sku: String, ignorarId: Long): Int

    @Insert
    suspend fun inserir(produto: ProdutoEntity): Long

    @Update
    suspend fun atualizar(produto: ProdutoEntity)

    @Delete
    suspend fun excluir(produto: ProdutoEntity)
}

@Dao
interface NotaFiscalDao {
    @Query("SELECT * FROM notas_fiscais ORDER BY data DESC, id DESC")
    fun observarTodas(): Flow<List<NotaFiscalEntity>>

    @Query("SELECT MAX(numero) FROM notas_fiscais")
    suspend fun maiorNumero(): Int?

    @Insert
    suspend fun inserir(nota: NotaFiscalEntity): Long
}

@Dao
interface LogSyncDao {
    @Query("SELECT * FROM logs_sync ORDER BY data DESC, id DESC")
    fun observarTodos(): Flow<List<LogSyncEntity>>

    @Insert
    suspend fun inserir(log: LogSyncEntity): Long
}

@Dao
interface PedidoDao {
    @Query("SELECT * FROM pedidos ORDER BY data DESC, id DESC")
    fun observarTodos(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos WHERE id = :id")
    suspend fun buscarPorId(id: Long): PedidoEntity?

    /** Pedidos de um status, do mais antigo para o mais novo (ordem natural de faturamento). */
    @Query("SELECT * FROM pedidos WHERE status = :status ORDER BY data ASC, id ASC")
    suspend fun buscarPorStatus(status: String): List<PedidoEntity>

    @Query("SELECT MAX(numeroPedido) FROM pedidos")
    suspend fun maiorNumero(): Int?

    @Insert
    suspend fun inserir(pedido: PedidoEntity): Long

    @Update
    suspend fun atualizar(pedido: PedidoEntity)
}