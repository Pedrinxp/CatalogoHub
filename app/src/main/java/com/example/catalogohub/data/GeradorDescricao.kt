package com.example.catalogohub.data

import com.example.catalogohub.model.DescricaoIA
import kotlinx.coroutines.delay
interface GeradorDescricao {
    suspend fun gerar(nomeProduto: String): DescricaoIA
}
class GeradorDescricaoLocal : GeradorDescricao {

    override suspend fun gerar(nomeProduto: String): DescricaoIA {
        delay(1200)

        val nome = nomeProduto.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { palavra -> palavra.replaceFirstChar { it.uppercase() } }

        val variacao = Math.floorMod(nome.lowercase().hashCode(), 3)

        val titulo = when (variacao) {
            0 -> "$nome Original | Qualidade Premium | Envio Rápido"
            1 -> "$nome - Pronta Entrega - Compra Segura"
            else -> "$nome | Ótimo Custo-Benefício | Garantia"
        }.take(60)

        val descricao = buildString {
            appendLine("$nome com acabamento de qualidade e ótimo custo-benefício.")
            appendLine()
            appendLine("• Produto novo, com nota fiscal")
            appendLine("• Envio rápido e embalagem reforçada")
            appendLine("• Ideal para o dia a dia")
            appendLine("• Dúvidas? Fale com a gente pelo chat")
            appendLine()
            append("Aproveite e garanta o seu hoje mesmo!")
        }

        return DescricaoIA(titulo = titulo, descricao = descricao)
    }
}
class GeradorDescricaoRemoto(
    private val alternativa: GeradorDescricao = GeradorDescricaoLocal()
) : GeradorDescricao {

    override suspend fun gerar(nomeProduto: String): DescricaoIA {
        return alternativa.gerar(nomeProduto)
    }
}
