package com.example.catalogohub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.catalogohub.data.LogSyncEntity
import com.example.catalogohub.data.NotaFiscalEntity
import com.example.catalogohub.model.Marketplace
import com.example.catalogohub.model.TipoLog
import com.example.catalogohub.model.comoDataHora
import com.example.catalogohub.model.comoMoeda
import com.example.catalogohub.model.comoNumeroNota
@Composable
fun NotasLogScreen(
    notas: List<NotaFiscalEntity>,
    logs: List<LogSyncEntity>,
    modifier: Modifier = Modifier
) {
    var secao by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BotaoSecao("Notas fiscais", secao == 0, Modifier.weight(1f)) { secao = 0 }
            BotaoSecao("Log de sincronização", secao == 1, Modifier.weight(1f)) { secao = 1 }
        }

        if (secao == 0) ListaNotas(notas) else ListaLogs(logs)
    }
}

@Composable
private fun BotaoSecao(
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
private fun ListaNotas(notas: List<NotaFiscalEntity>) {
    if (notas.isEmpty()) {
        EstadoVazio(
            icone = Icons.Default.Receipt,
            titulo = "Nenhuma nota emitida",
            texto = "Gere a primeira nota fiscal na aba Gerar NF, a partir de um pedido."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notas, key = { it.id }) { NotaCard(it) }
    }
}

@Composable
private fun NotaCard(nota: NotaFiscalEntity) {
    val canal = Marketplace.entries.firstOrNull { it.name == nota.canal }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = nota.numero.comoNumeroNota(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = nota.data.comoDataHora(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("Produto: ${nota.produtoNome}", style = MaterialTheme.typography.bodyMedium)
            Text("Quantidade: ${nota.quantidade}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Total: ${nota.total.comoMoeda()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (canal != null) CanalChip(canal)
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Documento simulado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ListaLogs(logs: List<LogSyncEntity>) {
    if (logs.isEmpty()) {
        EstadoVazio(
            icone = Icons.Default.Sync,
            titulo = "Sem registros ainda",
            texto = "Cadastros, vendas, pedidos e sincronizações de estoque aparecem aqui."
        )
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(logs, key = { it.id }) { LogItem(it) }
    }
}

@Composable
private fun LogItem(log: LogSyncEntity) {
    val tipo = runCatching { TipoLog.valueOf(log.tipo) }.getOrDefault(TipoLog.INFO)
    val icone: ImageVector = when (tipo) {
        TipoLog.SYNC -> Icons.Default.Sync
        TipoLog.VENDA -> Icons.Default.ShoppingCart
        TipoLog.PEDIDO -> Icons.Default.Inventory2
        TipoLog.NOTA -> Icons.Default.Receipt
        TipoLog.ESTOQUE -> Icons.Default.Warning
        TipoLog.INFO -> Icons.Default.Info
    }
    val alerta = tipo == TipoLog.ESTOQUE
    val corFundo = if (alerta) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val corIcone = if (alerta) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(corFundo),
                contentAlignment = Alignment.Center
            ) {
                Icon(icone, contentDescription = null, tint = corIcone, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(log.mensagem, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "${tipo.rotulo} · ${log.data.comoDataHora()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
