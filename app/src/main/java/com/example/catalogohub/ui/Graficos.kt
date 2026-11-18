package com.example.catalogohub.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catalogohub.model.Marketplace
import kotlin.math.max
import kotlin.math.min

/** Uma barra do gráfico de barras. */
data class BarraDado(
    val rotulo: String,
    val valor: Double,
    val cor: Color,
    val textoValor: String
)

/** Uma fatia da rosca. */
data class FatiaDado(
    val rotulo: String,
    val valor: Int,
    val cor: Color
)

/**
 * Cor do canal para usar em gráficos: no tema escuro, o preto do TikTok Shop
 * sumiria no fundo, então ele vira um cinza visível.
 */
@Composable
fun corNoGrafico(canal: Marketplace): Color {
    val escuro = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (escuro && canal.cor.luminance() < 0.1f) Color(0xFF9E9E9E) else canal.cor
}

/** Gráfico de barras verticais com o valor em cima de cada barra e o rótulo embaixo. */
@Composable
fun GraficoBarras(
    dados: List<BarraDado>,
    modifier: Modifier = Modifier,
    alturaGrafico: Dp = 190.dp
) {
    if (dados.isEmpty()) return

    val medidor = rememberTextMeasurer()
    val corTexto = MaterialTheme.colorScheme.onSurface
    val corBase = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val corContorno = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val estiloValor = TextStyle(color = corTexto, fontSize = 10.sp, fontWeight = FontWeight.Bold)

    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(alturaGrafico)
        ) {
            val maximo = dados.maxOf { it.valor }
            val espacoTopo = 20.dp.toPx()
            val areaAltura = size.height - espacoTopo
            val slot = size.width / dados.size
            val larguraBarra = slot * 0.6f
            val raio = CornerRadius(6.dp.toPx())

            // Linha de base
            drawLine(
                color = corBase,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )

            dados.forEachIndexed { i, d ->
                val x = i * slot + (slot - larguraBarra) / 2f
                val altura = if (maximo > 0.0 && d.valor > 0.0) {
                    max((d.valor / maximo).toFloat() * areaAltura, 3.dp.toPx())
                } else {
                    0f
                }
                val topo = size.height - altura

                if (altura > 0f) {
                    drawRoundRect(
                        color = d.cor,
                        topLeft = Offset(x, topo),
                        size = Size(larguraBarra, altura),
                        cornerRadius = raio
                    )
                    drawRoundRect(
                        color = corContorno,
                        topLeft = Offset(x, topo),
                        size = Size(larguraBarra, altura),
                        cornerRadius = raio,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Valor em R$ em cima da barra (centralizado e sem sair da tela)
                val texto = medidor.measure(d.textoValor, estiloValor)
                val tx = (x + larguraBarra / 2f - texto.size.width / 2f)
                    .coerceIn(0f, max(0f, size.width - texto.size.width))
                val ty = (topo - texto.size.height - 2.dp.toPx()).coerceAtLeast(0f)
                drawText(texto, topLeft = Offset(tx, ty))
            }
        }

        // Rótulos (um por barra, mesma largura de cada "fatia" do Canvas)
        Row(Modifier.fillMaxWidth()) {
            dados.forEach {
                Text(
                    text = it.rotulo,
                    modifier = Modifier.weight(1f),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Gráfico de rosca (donut) com um texto no centro. */
@Composable
fun GraficoRosca(
    fatias: List<FatiaDado>,
    textoCentro: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    tamanho: Dp = 170.dp
) {
    val corVazia = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier.size(tamanho), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val espessura = 26.dp.toPx()
            val diametro = min(size.width, size.height) - espessura
            val canto = Offset((size.width - diametro) / 2f, (size.height - diametro) / 2f)
            val tamanhoArco = Size(diametro, diametro)
            val visiveis = fatias.filter { it.valor > 0 }
            val total = visiveis.sumOf { it.valor }

            if (total <= 0) {
                drawArc(
                    color = corVazia,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = canto,
                    size = tamanhoArco,
                    style = Stroke(width = espessura)
                )
            } else {
                var inicio = -90f
                val folga = if (visiveis.size > 1) 2f else 0f
                visiveis.forEach { fatia ->
                    val angulo = fatia.valor.toFloat() / total * 360f
                    drawArc(
                        color = fatia.cor,
                        startAngle = inicio + folga / 2f,
                        sweepAngle = max(angulo - folga, 0.5f),
                        useCenter = false,
                        topLeft = canto,
                        size = tamanhoArco,
                        style = Stroke(width = espessura)
                    )
                    inicio += angulo
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = textoCentro,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Colunas do faturamento dia a dia (uma coluna por dia do mês). */
@Composable
fun GraficoColunasDias(
    valores: List<Double>,
    modifier: Modifier = Modifier,
    alturaGrafico: Dp = 170.dp
) {
    if (valores.isEmpty()) return

    val medidor = rememberTextMeasurer()
    val corColuna = MaterialTheme.colorScheme.primary
    val corGrade = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    val corVazio = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val estiloDia = TextStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 10.sp
    )

    Canvas(
        modifier
            .fillMaxWidth()
            .height(alturaGrafico)
    ) {
        val n = valores.size
        val maximo = valores.maxOrNull() ?: 0.0
        val alturaRotulo = 18.dp.toPx()
        val areaAltura = size.height - alturaRotulo
        val slot = size.width / n
        val larguraColuna = slot * 0.7f
        val raio = CornerRadius(3.dp.toPx())

        // Linhas de grade (base, metade e topo)
        listOf(0f, 0.5f, 1f).forEach { fracao ->
            val y = areaAltura * (1f - fracao)
            drawLine(
                color = corGrade,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        valores.forEachIndexed { i, valor ->
            val x = i * slot + (slot - larguraColuna) / 2f
            if (maximo > 0.0 && valor > 0.0) {
                val altura = max((valor / maximo).toFloat() * (areaAltura - 4.dp.toPx()), 3.dp.toPx())
                drawRoundRect(
                    color = corColuna,
                    topLeft = Offset(x, areaAltura - altura),
                    size = Size(larguraColuna, altura),
                    cornerRadius = raio
                )
            } else {
                // Dia sem vendas: só um tracinho
                drawRoundRect(
                    color = corVazio,
                    topLeft = Offset(x, areaAltura - 2.dp.toPx()),
                    size = Size(larguraColuna, 2.dp.toPx()),
                    cornerRadius = raio
                )
            }

            // Números dos dias: 1, 5, 10, 15... e o último (sem sobrepor)
            val dia = i + 1
            if (dia == 1 || dia == n || (dia % 5 == 0 && n - dia >= 3)) {
                val texto = medidor.measure(dia.toString(), estiloDia)
                val tx = (x + larguraColuna / 2f - texto.size.width / 2f)
                    .coerceIn(0f, max(0f, size.width - texto.size.width))
                drawText(texto, topLeft = Offset(tx, areaAltura + 4.dp.toPx()))
            }
        }
    }
}