package com.example.catalogohub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.catalogohub.model.DashboardUi
import com.example.catalogohub.model.comoMoeda
import com.example.catalogohub.model.comoMoedaCurta
import com.example.catalogohub.model.comoNomeMes
import com.example.catalogohub.model.comoPercentual

/**
 * Aba "Dashboard": indicadores e gráficos do mês escolhido.
 * Todos os números já chegam agregados do ViewModel.
 */
@Composable
fun DashboardScreen(
    dashboard: DashboardUi,
    onMesAnterior: () -> Unit,
    onProximoMes: () -> Unit,
    onGerarExemplos: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SeletorMes(
                nomeMes = dashboard.mes.comoNomeMes(),
                onAnterior = onMesAnterior,
                onProximo = onProximoMes
            )
        }

        if (dashboard.totalPedidos == 0) {
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    EstadoVazio(
                        icone = Icons.Default.Dashboard,
                        titulo = "Sem pedidos em ${dashboard.mes.comoNomeMes()}",
                        texto = "Simule vendas na aba Vendas ou gere pedidos de exemplo para ver os gráficos."
                    )
                    Button(onClick = onGerarExemplos, modifier = Modifier.height(52.dp)) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Gerar pedidos de exemplo")
                    }
                }
            }
        } else {
            // Indicadores
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IndicadorCard("Faturamento do mês", dashboard.faturamento.comoMoeda(), Modifier.weight(1f))
                        IndicadorCard("Pedidos do mês", dashboard.totalPedidos.toString(), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IndicadorCard("Ticket médio", dashboard.ticketMedio.comoMoeda(), Modifier.weight(1f))
                        IndicadorCard("Aguardando NF", dashboard.aguardandoNf.toString(), Modifier.weight(1f))
                    }
                }
            }

            // Faturamento por canal (barras)
            item {
                CartaoSecao("Faturamento por canal") {
                    GraficoBarras(
                        dados = dashboard.porCanal.map {
                            BarraDado(
                                rotulo = it.canal.nome,
                                valor = it.faturamento,
                                cor = corNoGrafico(it.canal),
                                textoValor = it.faturamento.comoMoedaCurta()
                            )
                        }
                    )
                }
            }

            // Pedidos por canal (rosca + legenda)
            item {
                CartaoSecao("Pedidos por canal") {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        GraficoRosca(
                            fatias = dashboard.porCanal.map {
                                FatiaDado(it.canal.nome, it.pedidos, corNoGrafico(it.canal))
                            },
                            textoCentro = dashboard.totalPedidos.toString(),
                            subtitulo = "pedidos"
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dashboard.porCanal.forEach { item ->
                            val cor = corNoGrafico(item.canal)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(cor)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.canal.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (item.pedidos == 1) "1 pedido" else "${item.pedidos} pedidos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Faturamento dia a dia
            item {
                CartaoSecao("Faturamento dia a dia") {
                    Text(
                        text = dashboard.faturamento.comoMoeda(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1
                    )
                    Text(
                        text = "Faturamento total em ${dashboard.mes.comoNomeMes()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    dashboard.melhorDia?.let {
                        Text(
                            text = "Melhor dia: ${it.dia} (${it.faturamento.comoMoeda()})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    GraficoColunasDias(valores = dashboard.porDia.map { it.faturamento })
                }
            }

            // Lista dos canais com valor e percentual
            item {
                CartaoSecao("Canais: valor e participação") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        dashboard.porCanal.forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MarcaCanal(item.canal, tamanho = 30.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.canal.nome,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = item.faturamento.comoMoeda(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth(item.percentual.coerceIn(0f, 1f))
                                                .fillMaxHeight()
                                                .background(corNoGrafico(item.canal))
                                        )
                                    }
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.percentual.comoPercentual(),
                                    style = MaterialTheme.typography.labelLarge,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    modifier = Modifier.width(60.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Seletor de mês: botão anterior, nome do mês ("Novembro/2026") e botão próximo. */
@Composable
private fun SeletorMes(nomeMes: String, onAnterior: () -> Unit, onProximo: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAnterior) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mês anterior")
            }
            Text(
                text = nomeMes,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onProximo) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Próximo mês")
            }
        }
    }
}

/** Card de indicador (rótulo pequeno + valor grande). */
@Composable
private fun IndicadorCard(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
            Text(
                text = rotulo,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}