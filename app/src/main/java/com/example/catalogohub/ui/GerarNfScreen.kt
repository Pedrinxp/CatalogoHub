package com.example.catalogohub.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.catalogohub.model.Marketplace
import com.example.catalogohub.model.comoDataHora
import com.example.catalogohub.model.comoMoeda
import com.example.catalogohub.model.comoNumeroNota
import com.example.catalogohub.model.comoNumeroPedido

@Composable
fun GerarNfScreen(
    pendentes: List<PedidoItemUi>,
    faturados: List<PedidoItemUi>,
    onGerarNf: (Long) -> Unit,
    onGerarTodas: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secao by rememberSaveable { mutableIntStateOf(0) }
    var confirmando by rememberSaveable { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotaoAbaNf("Pendentes (${pendentes.size})", secao == 0, Modifier.weight(1f)) { secao = 0 }
            BotaoAbaNf("Faturados (${faturados.size})", secao == 1, Modifier.weight(1f)) { secao = 1 }
        }

        if (secao == 0) {
            ListaPendentes(
                pendentes = pendentes,
                onGerarNf = onGerarNf,
                onGerarTodas = { confirmando = true }
            )
        } else {
            ListaFaturados(faturados)
        }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Gerar todas as NFs?") },
            text = {
                Text("Serão geradas ${pendentes.size} nota(s) fiscal(is) simulada(s) e os pedidos passarão para Faturado.")
            },
            confirmButton = {
                Button(onClick = {
                    confirmando = false
                    onGerarTodas()
                }) { Text("Gerar todas") }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun BotaoAbaNf(
    titulo: String,
    selecionado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val botaoModifier = modifier.height(48.dp)
    if (selecionado) {
        Button(onClick = onClick, modifier = botaoModifier) { Text(titulo, maxLines = 1) }
    } else {
        OutlinedButton(onClick = onClick, modifier = botaoModifier) { Text(titulo, maxLines = 1) }
    }
}

@Composable
private fun ListaPendentes(
    pendentes: List<PedidoItemUi>,
    onGerarNf: (Long) -> Unit,
    onGerarTodas: () -> Unit
) {
    if (pendentes.isEmpty()) {
        EstadoVazio(
            icone = Icons.Default.CheckCircle,
            titulo = "Nenhum pedido pendente",
            texto = "Quando houver vendas aguardando nota fiscal, elas aparecem aqui."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Button(
                onClick = onGerarTodas,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("Gerar todas as NFs pendentes", fontWeight = FontWeight.Bold)
            }
        }
        items(pendentes, key = { it.pedido.id }) { item ->
            PedidoCard(item = item, onGerarNf = { onGerarNf(item.pedido.id) })
        }
    }
}

@Composable
private fun ListaFaturados(faturados: List<PedidoItemUi>) {
    if (faturados.isEmpty()) {
        EstadoVazio(
            icone = Icons.Default.Receipt,
            titulo = "Nenhum pedido faturado",
            texto = "Os pedidos aparecem aqui, com o número da NF, depois de gerar a nota."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(faturados, key = { it.pedido.id }) { item ->
            PedidoCard(item = item, onGerarNf = null)
        }
    }
}
@Composable
private fun PedidoCard(item: PedidoItemUi, onGerarNf: (() -> Unit)?) {
    val pedido = item.pedido
    val canal = Marketplace.entries.firstOrNull { it.name == pedido.canal }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FotoProduto(
                    nome = pedido.produtoNome,
                    foto = item.foto,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = pedido.produtoNome,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (canal != null) CanalChip(canal)
                    Text(
                        text = "${pedido.numeroPedido.comoNumeroPedido()} · ${pedido.data.comoDataHora()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Quantidade: ${pedido.quantidade}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = pedido.total.comoMoeda(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (onGerarNf != null) {
                Button(
                    onClick = onGerarNf,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Gerar NF", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "Faturado · ${pedido.numeroNota?.comoNumeroNota() ?: "NF não encontrada"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
