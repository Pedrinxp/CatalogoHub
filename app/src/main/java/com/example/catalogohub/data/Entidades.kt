package com.example.catalogohub.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Produto do catálogo central.
 * - foto: "res:nome_do_drawable" (imagem de exemplo) ou caminho de arquivo copiado da galeria.
 * - canais: nomes dos marketplaces separados por vírgula (ex.: "SHOPEE,MAGALU").
 */
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

/** Nota fiscal simulada, emitida na aba "Gerar NF" a partir de um pedido. */
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

/** Registro do histórico de sincronização. "tipo" guarda o nome do enum TipoLog. */
@Entity(tableName = "logs_sync")
data class LogSyncEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mensagem: String,
    val tipo: String,
    val data: Long
)

/**
 * Pedido gerado por uma venda em um canal.
 * - canal: nome do enum Marketplace.
 * - status: nome do enum StatusPedido (AGUARDANDO_NF ou FATURADO).
 * - numeroNota: preenchido quando a NF é gerada.
 */
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