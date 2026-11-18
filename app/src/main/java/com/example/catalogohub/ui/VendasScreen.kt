package com.example.catalogohub.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.catalogohub.data.ProdutoEntity
import com.example.catalogohub.model.Marketplace
import com.example.catalogohub.model.comoMoeda
import com.example.catalogohub.model.paraMarketplaces
@Composable
fun VendasScreen(
    produtos: List<ProdutoEntity>,
    onVender: (Long, Marketplace, Int) -> Unit,
    onGerarExemplos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var vendendoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val produtoEmVenda = produtos.firstOrNull { it.id == vendendoId }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Simule uma venda em qualquer canal. O estoque é baixado em todos os marketplaces e um pedido é criado, aguardando NF. A nota fiscal é gerada na aba Gerar NF.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedButton(
                onClick = onGerarExemplos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Gerar pedidos de exemplo")
            }
        }

        if (produtos.isEmpty()) {
            item {
                EstadoVazio(
                    icone = Icons.Default.Inventory2,
                    titulo = "Sem produtos para vender",
                    texto = "Cadastre um produto na aba Produtos para começar a simular vendas."
                )
            }
        } else {
            items(produtos, key = { it.id }) { produto ->
                VendaCard(produto = produto, onVender = { vendendoId = produto.id })
            }
        }
    }

    if (produtoEmVenda != null) {
        VendaDialog(
            produto = produtoEmVenda,
            onDismiss = { vendendoId = null },
            onConfirmar = { canal, quantidade ->
                onVender(produtoEmVenda.id, canal, quantidade)
                vendendoId = null
            }
        )
    }
}

@Composable
private fun VendaCard(produto: ProdutoEntity, onVender: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FotoProduto(
                nome = produto.nome,
                foto = produto.foto,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = produto.nome,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = produto.preco.comoMoeda(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                EstoqueBadge(produto.estoque)
            }
            Spacer(Modifier.size(8.dp))
            Button(
                onClick = onVender,
                enabled = produto.estoque > 0,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Vender")
            }
        }
    }
}

@Composable
private fun VendaDialog(
    produto: ProdutoEntity,
    onDismiss: () -> Unit,
    onConfirmar: (Marketplace, Int) -> Unit
) {
    val canais = remember(produto.canais) { produto.canais.paraMarketplaces() }
    var canalEscolhido by remember { mutableStateOf(canais.firstOrNull()) }
    var quantidade by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Simular venda") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(produto.nome, fontWeight = FontWeight.Bold)
                Text(
                    "Disponível: ${produto.estoque} un.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text("Canal da venda", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    canais.forEach { canal ->
                        CanalChip(
                            canal = canal,
                            selecionado = canal == canalEscolhido,
                            onClick = { canalEscolhido = canal }
                        )
                    }
                }

                Text("Quantidade", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalIconButton(onClick = { if (quantidade > 1) quantidade-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir")
                    }
                    Text(
                        text = quantidade.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    FilledTonalIconButton(onClick = { if (quantidade < produto.estoque) quantidade++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar")
                    }
                }

                Text(
                    text = "Total: ${(produto.preco * quantidade).comoMoeda()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { canalEscolhido?.let { onConfirmar(it, quantidade) } },
                enabled = canalEscolhido != null && quantidade in 1..produto.estoque
            ) { Text("Confirmar venda") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
