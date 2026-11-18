package com.example.catalogohub.data

import com.example.catalogohub.model.DescricaoIA
import kotlinx.coroutines.delay

/**
 * Contrato do gerador de anúncios por IA.
 * A tela e o ViewModel só conhecem esta interface, então trocar a versão simulada
 * por uma API real (Claude, Gemini...) não exige mudar mais nada.
 */
interface GeradorDescricao {
    suspend fun gerar(nomeProduto: String): DescricaoIA
}

/** Versão SIMULADA: gera título e descrição localmente, sem internet. */
class GeradorDescricaoLocal : GeradorDescricao {

    override suspend fun gerar(nomeProduto: String): DescricaoIA {
        delay(1200) // simula o tempo de resposta de uma API

        val nome = nomeProduto.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { palavra -> palavra.replaceFirstChar { it.uppercase() } }

        val variacao = Math.floorMod(nome.lowercase().hashCode(), 3)

        val titulo = when (variacao) {
            0 -> "$nome Original | Qualidade Premium | Envio Rápido"
            1 -> "$nome - Pronta Entrega - Compra Segura"
            else -> "$nome | Ótimo Custo-Benefício | Garantia"
        }.take(60) // limite comum de título nos marketplaces

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

/**
 * Esqueleto para uma API real. Para ligar:
 * 1) adicione <uses-permission android:name="android.permission.INTERNET" /> no AndroidManifest;
 * 2) faça a chamada HTTP abaixo (de preferência para o SEU servidor, que guarda a chave da API;
 *    nunca coloque a chave dentro do app);
 * 3) no CatalogoViewModel, troque GeradorDescricaoLocal() por GeradorDescricaoRemoto().
 */
class GeradorDescricaoRemoto(
    private val alternativa: GeradorDescricao = GeradorDescricaoLocal()
) : GeradorDescricao {

    override suspend fun gerar(nomeProduto: String): DescricaoIA {
        // TODO: enviar "nomeProduto" para a API, pedir título (máx. 60 caracteres) e descrição,
        //       e converter a resposta em DescricaoIA.
        // Enquanto isso, usa a versão local para o app continuar funcionando.
        return alternativa.gerar(nomeProduto)
    }
}