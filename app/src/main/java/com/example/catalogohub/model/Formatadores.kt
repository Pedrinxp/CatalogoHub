package com.example.catalogohub.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
private val localeBR: Locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()

private val nomesMeses = listOf(
    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
)
fun Double.comoMoeda(): String = NumberFormat.getCurrencyInstance(localeBR).format(this)
fun Double.comoMoedaCurta(): String = when {
    this >= 1_000_000 -> "R\$ " + "%.1f".format(localeBR, this / 1_000_000) + " mi"
    this >= 1_000 -> "R\$ " + "%.1f".format(localeBR, this / 1_000) + " mil"
    else -> "R\$ " + "%.0f".format(localeBR, this)
}
fun Float.comoPercentual(): String = "%.1f%%".format(localeBR, this * 100f)

fun Long.comoDataHora(): String = SimpleDateFormat("dd/MM/yyyy HH:mm", localeBR).format(Date(this))

fun Int.comoNumeroNota(): String = "NF-" + toString().padStart(6, '0')

fun Int.comoNumeroPedido(): String = "PED-" + toString().padStart(6, '0')

fun MesRef.comoNomeMes(): String = "${nomesMeses[mes - 1]}/$ano"
