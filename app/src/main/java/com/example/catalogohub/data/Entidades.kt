package com.example.catalogohub.data

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "produtos")
data class ProdutoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val sku: String,
    val preco: Double,
    val estoque: Int,
    val foto: String? = null,
    val descricao: String = "",
    val canais: String = ""
)
@Entity(tableName = "notas_fiscais")
data class NotaFiscalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numero: Int,
    val produtoId: Long,
    val produtoNome: String,
    val quantidade: Int,
    val total: Double,
    val canal: String,
    val data: Long
)
@Entity(tableName = "logs_sync")
data class LogSyncEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mensagem: String,
    val tipo: String,
    val data: Long
)
@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numeroPedido: Int,
    val produtoId: Long,
    val produtoNome: String,
    val quantidade: Int,
    val total: Double,
    val canal: String,
    val data: Long,
    val status: String,
    val numeroNota: Int? = null
)
