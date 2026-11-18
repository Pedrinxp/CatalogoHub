package com.example.catalogohub.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.catalogohub.data.ArmazenamentoImagens
import com.example.catalogohub.model.LIMITE_ESTOQUE_BAIXO
import com.example.catalogohub.model.Marketplace
import com.example.catalogohub.model.ResumoUi
import com.example.catalogohub.model.comoMoeda
import com.example.catalogohub.ui.theme.Turquesa
import com.example.catalogohub.ui.theme.VerdeEscuro

private val FundoDegrade = Brush.horizontalGradient(listOf(VerdeEscuro, Turquesa))
private val FormaCabecalho = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

/** Cores padrão dos cards do app. */
@Composable
fun coresCartao(): CardColors =
    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

/** Cabeçalho com degradê verde/turquesa e os 4 cards de resumo. */
@Composable
fun CabecalhoResumo(resumo: ResumoUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FormaCabecalho)
            .background(FundoDegrade)
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
    ) {
        Text(
            text = "CatalogoHub",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Todos os seus canais em um só lugar",
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardResumo("Produtos", resumo.totalProdutos.toString(), Modifier.weight(1f))
            CardResumo("Unidades em estoque", resumo.totalUnidades.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CardResumo("Pedidos", resumo.pedidos.toString(), Modifier.weight(1f))
            CardResumo("Total vendido", resumo.valorVendido.comoMoeda(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun CardResumo(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = rotulo,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 12.sp,
                maxLines = 1
            )
            Text(
                text = valor,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

/** Cabeçalho simples (usado na tela de cadastro/edição). */
@Composable
fun CabecalhoSimples(titulo: String, onVoltar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FormaCabecalho)
            .background(FundoDegrade)
            .statusBarsPadding()
            .padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVoltar) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }
        Text(
            text = titulo,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Card de seção (título + conteúdo), usado nos gráficos e listas do Dashboard. */
@Composable
fun CartaoSecao(
    titulo: String,
    modifier: Modifier = Modifier,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            conteudo()
        }
    }
}

/** Foto do produto; se não houver foto, mostra a inicial do nome. */
@Composable
fun FotoProduto(
    nome: String,
    foto: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val modelo = remember(foto) { ArmazenamentoImagens.resolverModelo(context, foto) }
    val forma = RoundedCornerShape(16.dp)
    val base = modifier
        .clip(forma)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    if (modelo != null) {
        AsyncImage(
            model = modelo,
            contentDescription = nome,
            contentScale = ContentScale.Crop,
            modifier = base
        )
    } else {
        Box(
            modifier = base.background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nome.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/** "Logo" redondo do marketplace (círculo colorido com a inicial). */
@Composable
fun MarcaCanal(canal: Marketplace, modifier: Modifier = Modifier, tamanho: Dp = 32.dp) {
    Box(
        modifier = modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(canal.cor)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = canal.inicial,
            color = canal.corTexto,
            fontWeight = FontWeight.Bold,
            fontSize = (tamanho.value * 0.45f).sp
        )
    }
}

/** Chip colorido do marketplace. Se "onClick" for informado, vira um botão de seleção. */
@Composable
fun CanalChip(
    canal: Marketplace,
    modifier: Modifier = Modifier,
    selecionado: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val forma = RoundedCornerShape(50)
    val fundo = if (selecionado) canal.cor else MaterialTheme.colorScheme.surfaceVariant
    val corTexto = if (selecionado) canal.corTexto else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = modifier,
        shape = forma,
        color = fundo,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Text(
            text = canal.nome,
            color = corTexto,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            modifier = Modifier
                .then(if (onClick != null) Modifier.clip(forma).clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

/** Selo de estoque: verde normal, vermelho com alerta quando for 5 unidades ou menos. */
@Composable
fun EstoqueBadge(estoque: Int, modifier: Modifier = Modifier) {
    val baixo = estoque <= LIMITE_ESTOQUE_BAIXO
    val fundo = when {
        estoque <= 0 -> MaterialTheme.colorScheme.error
        baixo -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val conteudo = when {
        estoque <= 0 -> MaterialTheme.colorScheme.onError
        baixo -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val texto = when {
        estoque <= 0 -> "Esgotado"
        baixo -> "Estoque baixo: $estoque un."
        else -> "$estoque un. em estoque"
    }

    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = fundo) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (baixo) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = conteudo,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.size(4.dp))
            }
            Text(
                text = texto,
                color = conteudo,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Estado vazio amigável. */
@Composable
fun EstadoVazio(
    icone: ImageVector,
    titulo: String,
    texto: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icone,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}