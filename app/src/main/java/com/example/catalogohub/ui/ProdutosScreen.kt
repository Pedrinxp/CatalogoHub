package com.example.catalogohub.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.catalogohub.data.ProdutoEntity
import com.example.catalogohub.model.LIMITE_ESTOQUE_BAIXO
import com.example.catalogohub.model.comoMoeda
import com.example.catalogohub.model.paraMarketplaces

@Composable
fun ProdutosScreen(
    produtos: List<ProdutoEntity>,
    totalCadastrados: Int,
    busca: String,
    onBuscaChange: (String) -> Unit,
    onEditar: (ProdutoEntity) -> Unit,
    onExcluir: (ProdutoEntity) -> Unit,
    onAtualizarFoto: (Long, Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    var paraExcluir by remember { mutableStateOf<ProdutoEntity?>(null) }
    var produtoDaFotoId by rememberSaveable { mutableStateOf<Long?>(null) }
    val seletorDeFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val id = produtoDaFotoId
        if (uri != null && id != null) onAtualizarFoto(id, uri)
        produtoDaFotoId = null
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { CampoBusca(busca, onBuscaChange) }

        if (produtos.isEmpty()) {
            item {
                if (totalCadastrados == 0) {
                    EstadoVazio(
                        icone = Icons.Default.Inventory2,
                        titulo = "Nenhum produto ainda",
                        texto = "Toque em \"Novo produto\" para cadastrar o primeiro item e escolher em quais canais ele será publicado."
                    )
                } else {
                    EstadoVazio(
                        icone = Icons.Default.Search,
                        titulo = "Nada encontrado",
                        texto = "Tente buscar por outro nome ou SKU."
                    )
                }
            }
        } else {
            items(produtos, key = { it.id }) { produto ->
                ProdutoCard(
                    produto = produto,
                    onEditar = { onEditar(produto) },
                    onExcluir = { paraExcluir = produto },
                    onTrocarFoto = {
                        produtoDaFotoId = produto.id
                        seletorDeFoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }
    paraExcluir?.let { produto ->
        AlertDialog(
            onDismissRequest = { paraExcluir = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Excluir produto?") },
            text = {
                Text("\"${produto.nome}\" será removido de todos os canais. As notas fiscais já emitidas continuam no histórico.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onExcluir(produto)
                        paraExcluir = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { paraExcluir = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun CampoBusca(busca: String, onBuscaChange: (String) -> Unit) {
    OutlinedTextField(
        value = busca,
        onValueChange = onBuscaChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar por nome ou SKU") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (busca.isNotEmpty()) {
                IconButton(onClick = { onBuscaChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun ProdutoCard(
    produto: ProdutoEntity,
    onEditar: () -> Unit,
    onExcluir: () -> Unit,
    onTrocarFoto: () -> Unit
) {
    val canais = remember(produto.canais) { produto.canais.paraMarketplaces() }
    val estoqueBaixo = produto.estoque <= LIMITE_ESTOQUE_BAIXO

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = coresCartao(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (estoqueBaixo) BorderStroke(1.5.dp, MaterialTheme.colorScheme.error) else null
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box {
                    FotoProduto(
                        nome = produto.nome,
                        foto = produto.foto,
                        modifier = Modifier.size(92.dp),
                        onClick = onTrocarFoto
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Trocar foto",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.size(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = produto.nome,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "SKU: ${produto.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = produto.preco.comoMoeda(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(6.dp))
                    EstoqueBadge(produto.estoque)
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                canais.forEach { CanalChip(it) }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Editar")
                }
                OutlinedButton(
                    onClick = onExcluir,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Excluir")
                }
            }
        }
    }
}
