package com.example.catalogohub.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.catalogohub.data.ProdutoEntity
import com.example.catalogohub.model.Marketplace
import com.example.catalogohub.model.paraMarketplaces
import com.example.catalogohub.model.paraTexto

/**
 * Tela de cadastro (produto == null) e de edição (produto != null).
 * Na edição, SKU, estoque e canais ficam bloqueados: só nome, preço, descrição e foto mudam.
 */
@Composable
fun ProdutoFormScreen(
    produto: ProdutoEntity?,
    ia: IaState,
    onGerarIa: (String) -> Unit,
    onLimparIa: () -> Unit,
    onSalvarNovo: (
        nome: String, sku: String, preco: String, estoque: String,
        descricao: String, foto: Uri?, canais: List<Marketplace>
    ) -> Unit,
    onSalvarEdicao: (id: Long, nome: String, preco: String, descricao: String, foto: Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val editando = produto != null

    var nome by rememberSaveable(produto?.id) { mutableStateOf(produto?.nome ?: "") }
    var sku by rememberSaveable(produto?.id) { mutableStateOf(produto?.sku ?: "") }
    var preco by rememberSaveable(produto?.id) { mutableStateOf(produto?.preco?.toString() ?: "") }
    var estoque by rememberSaveable(produto?.id) { mutableStateOf(produto?.estoque?.toString() ?: "") }
    var descricao by rememberSaveable(produto?.id) { mutableStateOf(produto?.descricao ?: "") }
    var canaisTexto by rememberSaveable(produto?.id) { mutableStateOf(produto?.canais ?: "") }
    var fotoUri by rememberSaveable(produto?.id) { mutableStateOf<Uri?>(null) }

    val canaisSelecionados = canaisTexto.paraMarketplaces()

    val seletorDeFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) fotoUri = uri }

    val escolherFoto: () -> Unit = {
        seletorDeFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    val alternarCanal: (Marketplace) -> Unit = { canal ->
        val novos = if (canal in canaisSelecionados) canaisSelecionados - canal else canaisSelecionados + canal
        canaisTexto = Marketplace.entries.filter { it in novos }.paraTexto()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- Foto ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            val tamanhoFoto = Modifier.size(112.dp)
            if (fotoUri != null) {
                AsyncImage(
                    model = fotoUri,
                    contentDescription = "Foto escolhida",
                    contentScale = ContentScale.Crop,
                    modifier = tamanhoFoto
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = escolherFoto)
                )
            } else {
                FotoProduto(
                    nome = nome,
                    foto = produto?.foto,
                    modifier = tamanhoFoto,
                    onClick = escolherFoto
                )
            }
            Spacer(Modifier.size(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Foto do produto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Toque na foto para escolher uma imagem da galeria.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = escolherFoto) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Escolher foto")
                }
            }
        }

        // ---------- Campos ----------
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome do produto") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            shape = RoundedCornerShape(16.dp)
        )

        OutlinedTextField(
            value = sku,
            onValueChange = { sku = it },
            label = { Text("SKU (código único)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !editando,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            shape = RoundedCornerShape(16.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = preco,
                onValueChange = { preco = it },
                label = { Text("Preço (R$)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp)
            )
            OutlinedTextField(
                value = estoque,
                onValueChange = { estoque = it },
                label = { Text("Estoque") },
                modifier = Modifier.weight(1f),
                enabled = !editando,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp)
            )
        }

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição do anúncio (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(16.dp)
        )

        // ---------- IA ----------
        FilledTonalButton(
            onClick = { onGerarIa(nome) },
            enabled = nome.isNotBlank() && !ia.gerando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (ia.gerando) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                Spacer(Modifier.size(10.dp))
                Text("Gerando...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Gerar descrição com IA")
            }
        }

        ia.sugestao?.let { sugestao ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Sugestão da IA",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        sugestao.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        sugestao.descricao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                nome = sugestao.titulo
                                descricao = sugestao.descricao
                                onLimparIa()
                            }
                        ) { Text("Usar sugestão") }
                        TextButton(onClick = onLimparIa) { Text("Descartar") }
                    }
                }
            }
        }

        // ---------- Canais ----------
        Text(
            if (editando) "Canais de venda (bloqueado na edição)" else "Publicar nos canais",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = coresCartao(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Marketplace.entries.forEach { canal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !editando) { alternarCanal(canal) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MarcaCanal(canal)
                        Spacer(Modifier.size(12.dp))
                        Text(
                            canal.nome,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = canal in canaisSelecionados,
                            onCheckedChange = null,
                            enabled = !editando
                        )
                    }
                }
            }
        }

        // ---------- Salvar ----------
        Button(
            onClick = {
                if (produto != null) {
                    onSalvarEdicao(produto.id, nome, preco, descricao, fotoUri)
                } else {
                    onSalvarNovo(nome, sku, preco, estoque, descricao, fotoUri, canaisSelecionados)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text(
                if (editando) "Salvar alterações" else "Salvar produto",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}