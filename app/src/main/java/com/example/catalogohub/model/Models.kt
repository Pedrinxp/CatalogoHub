package com.example.catalogohub.model

import java.util.Calendar
const val LIMITE_ESTOQUE_BAIXO = 5
data class ResumoUi(
    val totalProdutos: Int = 0,
    val totalUnidades: Int = 0,
    val pedidos: Int = 0,
    val valorVendido: Double = 0.0
)
data class DescricaoIA(
    val titulo: String,
    val descricao: String
)
enum class TipoLog(val rotulo: String) {
    SYNC("Sincronização"),
    VENDA("Venda"),
    PEDIDO("Pedido"),
    NOTA("Nota fiscal"),
    ESTOQUE("Alerta de estoque"),
    INFO("Informação")
}
enum class StatusPedido(val rotulo: String) {
    AGUARDANDO_NF("Aguardando NF"),
    FATURADO("Faturado")
}
sealed interface ResultadoVenda {
    data class Sucesso(val numeroPedido: Int) : ResultadoVenda
    data class Erro(val mensagem: String) : ResultadoVenda
}
sealed interface ResultadoNf {
    data class Sucesso(val numeroNota: Int) : ResultadoNf
    data class Erro(val mensagem: String) : ResultadoNf
}
data class MesRef(val ano: Int, val mes: Int) {
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
data class TotalCanal(
    val canal: Marketplace,
    val faturamento: Double,
    val pedidos: Int,
    val percentual: Float
)
data class TotalDia(
    val dia: Int,
    val faturamento: Double
)
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
