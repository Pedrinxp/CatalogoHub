package com.example.catalogohub.model

import androidx.compose.ui.graphics.Color

/**
 * Canais de venda suportados pelo app.
 * Cada canal tem nome, inicial (usada como "logo") e cores.
 */
enum class Marketplace(
    val nome: String,
    val inicial: String,
    val cor: Color,
    val corTexto: Color
) {
    MERCADO_LIVRE("Mercado Livre", "M", Color(0xFFFFE600), Color(0xFF2D3277)),
    MAGALU("Magalu", "M", Color(0xFF0086FF), Color.White),
    SHOPEE("Shopee", "S", Color(0xFFEE4D2D), Color.White),
    TIKTOK_SHOP("TikTok Shop", "T", Color(0xFF000000), Color.White),
    LOJA_PROPRIA("Loja própria", "L", Color(0xFF2E9E5B), Color.White)
}

/** Converte o texto salvo no banco ("SHOPEE,MAGALU") em lista de canais. */
fun String.paraMarketplaces(): List<Marketplace> =
    split(",").mapNotNull { texto -> Marketplace.entries.firstOrNull { it.name == texto.trim() } }

/** Converte a lista de canais em texto para salvar no banco. */
fun List<Marketplace>.paraTexto(): String = joinToString(",") { it.name }

