package com.example.catalogohub.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Locale pt-BR criado com Builder (os construtores de Locale estão depreciados nas versões novas do Java)
private val localeBR: Locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()

private val nomesMeses = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)

/** 59.9 -> "R$ 59,90" */
fun Double.comoMoeda(): String = NumberFormat.getCurrencyInstance(localeBR).format(this)

/** Versão curta para rótulos de gráfico: 950.0 -> "R$ 950", 1234.0 -> "R$ 1,2 mil". */
fun Double.comoMoedaCurta(): String = when {
    this >= 1_000_000 -> "R\$ " + "%.1f".format(localeBR, this / 1_000_000) + " mi"
    this >= 1_000 -> "R\$ " + "%.1f".format(localeBR, this / 1_000) + " mil"
    else -> "R\$ " + "%.0f".format(localeBR, this)
}

/** 0.256f -> "25,6%" (recebe uma fração de 0 a 1) */
fun Float.comoPercentual(): String = "%.1f%%".format(localeBR, this * 100f)

/** Timestamp em milissegundos -> "17/11/2026 20:30" */
fun Long.comoDataHora(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", localeBR).format(Date(this))

/** 12 -> "NF-000012" */
fun Int.comoNumeroNota(): String = "NF-" + toString().padStart(6, '0')

/** 7 -> "PED-000007" */
fun Int.comoNumeroPedido(): String = "PED-" + toString().padStart(6, '0')

/** MesRef(2026, 11) -> "Novembro/2026" */
fun MesRef.comoNomeMes(): String = "${nomesMeses[mes - 1]}/$ano"