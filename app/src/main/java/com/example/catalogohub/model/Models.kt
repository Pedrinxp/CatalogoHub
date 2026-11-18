package com.example.catalogohub.model

import java.util.Calendar

/** Quantidade a partir da qual o estoque é considerado baixo (5 ou menos). */
const val LIMITE_ESTOQUE_BAIXO = 5

/** Dados do resumo exibido no topo do app. */
data class ResumoUi(
    val totalProdutos: Int = 0,
    val totalUnidades: Int = 0,
    val pedidos: Int = 0,
    val valorVendido: Double = 0.0
)

/** Sugestão de anúncio gerada pela IA. */
data class DescricaoIA(
    val titulo: String,
    val descricao: String
)

/** Tipos de registro do log de sincronização. */
enum class TipoLog(val rotulo: String) {
    SYNC("Sincronização"),
    VENDA("Venda"),
    PEDIDO("Pedido"),
    NOTA("Nota fiscal"),
    ESTOQUE("Alerta de estoque"),
    INFO("Informação")
}

/** Situação de um pedido. */
enum class StatusPedido(val rotulo: String) {
    AGUARDANDO_NF("Aguardando NF"),
    FATURADO("Faturado")
}

/** Resultado de uma tentativa de venda (a venda gera um pedido). */
sealed interface ResultadoVenda {
    data class Sucesso(val numeroPedido: Int) : ResultadoVenda
    data class Erro(val mensagem: String) : ResultadoVenda
}

/** Resultado de uma tentativa de gerar NF para um pedido. */
sealed interface ResultadoNf {
    data class Sucesso(val numeroNota: Int) : ResultadoNf
    data class Erro(val mensagem: String) : ResultadoNf
}

/** Mês de referência do dashboard. "mes" vai de 1 a 12. */
data class MesRef(val ano: Int, val mes: Int) {

    /** Anda "delta" meses (-1 = mês anterior, +1 = próximo mês). */
    fun deslocar(delta: Int): MesRef {
        val indice = ano * 12 + (mes - 1) + delta
        return MesRef(indice / 12, indice % 12 + 1)
    }

    companion object {
        fun atual(): MesRef {
            val cal = Calendar.getInstance()
            return MesRef(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }
    }
}

/** Faturamento e quantidade de pedidos de um canal no mês. "percentual" vai de 0f a 1f. */
data class TotalCanal(
    val canal: Marketplace,
    val faturamento: Double,
    val pedidos: Int,
    val percentual: Float
)

/** Faturamento de um dia do mês. */
data class TotalDia(
    val dia: Int,
    val faturamento: Double
)

/** Todos os números do dashboard para o mês escolhido (já agregados). */
data class DashboardUi(
    val mes: MesRef = MesRef.atual(),
    val faturamento: Double = 0.0,
    val totalPedidos: Int = 0,
    val ticketMedio: Double = 0.0,
    val aguardandoNf: Int = 0,
    val porCanal: List<TotalCanal> = emptyList(),
    val porDia: List<TotalDia> = emptyList(),
    val melhorDia: TotalDia? = null
)