package com.example.catalogohub.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

/** As 5 abas, na ordem da barra de navegação. */
private enum class Aba(val titulo: String, val icone: ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    PRODUTOS("Produtos", Icons.Default.Inventory2),
    VENDAS("Vendas", Icons.Default.ShoppingCart),
    GERAR_NF("Gerar NF", Icons.Default.RequestQuote),
    NOTAS("Notas/Log", Icons.Default.Receipt)
}

/** Raiz do app: cabeçalho, navegação inferior, FAB, Snackbar e as telas. */
@Composable
fun CatalogoApp(viewModel: CatalogoViewModel = viewModel()) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboard by viewModel.dashboard.collectAsStateWithLifecycle()
    val ia by viewModel.ia.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var abaAtual by rememberSaveable { mutableIntStateOf(0) }
    var formAberto by rememberSaveable { mutableStateOf(false) }
    var editandoId by rememberSaveable { mutableStateOf<Long?>(null) }

    val produtoEditando = estado.todosProdutos.firstOrNull { it.id == editandoId }

    // Mostra as mensagens do ViewModel em Snackbar (a mais nova substitui a anterior)
    LaunchedEffect(Unit) {
        viewModel.mensagens.collect { mensagem ->
            snackbarHostState.currentSnackbarData?.dismiss()
            launch { snackbarHostState.showSnackbar(mensagem) }
        }
    }

    // Botão voltar do celular fecha o formulário
    BackHandler(enabled = formAberto) { formAberto = false }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (formAberto) {
                CabecalhoSimples(
                    titulo = if (produtoEditando != null) "Editar produto" else "Novo produto",
                    onVoltar = { formAberto = false }
                )
            } else {
                CabecalhoResumo(estado.resumo)
            }
        },
        bottomBar = {
            if (!formAberto) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Aba.entries.forEachIndexed { indice, aba ->
                        NavigationBarItem(
                            selected = abaAtual == indice,
                            onClick = { abaAtual = indice },
                            icon = { Icon(aba.icone, contentDescription = aba.titulo) },
                            label = {
                                Text(
                                    text = aba.titulo,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // O FAB "Novo produto" aparece só na aba Produtos
            if (!formAberto && abaAtual == Aba.PRODUTOS.ordinal) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editandoId = null
                        viewModel.limparSugestao()
                        formAberto = true
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Novo produto") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (formAberto) {
                ProdutoFormScreen(
                    produto = produtoEditando,
                    ia = ia,
                    onGerarIa = viewModel::gerarDescricao,
                    onLimparIa = viewModel::limparSugestao,
                    onSalvarNovo = { nome, sku, preco, estoque, descricao, foto, canais ->
                        viewModel.criarProduto(nome, sku, preco, estoque, descricao, foto, canais) {
                            formAberto = false
                        }
                    },
                    onSalvarEdicao = { id, nome, preco, descricao, foto ->
                        viewModel.editarProduto(id, nome, preco, descricao, foto) {
                            formAberto = false
                        }
                    }
                )
            } else {
                when (Aba.entries[abaAtual]) {
                    Aba.DASHBOARD -> DashboardScreen(
                        dashboard = dashboard,
                        onMesAnterior = { viewModel.mudarMes(-1) },
                        onProximoMes = { viewModel.mudarMes(1) },
                        onGerarExemplos = viewModel::gerarPedidosExemplo
                    )

                    Aba.PRODUTOS -> ProdutosScreen(
                        produtos = estado.produtos,
                        totalCadastrados = estado.todosProdutos.size,
                        busca = estado.busca,
                        onBuscaChange = viewModel::onBuscaChange,
                        onEditar = { produto ->
                            editandoId = produto.id
                            viewModel.limparSugestao()
                            formAberto = true
                        },
                        onExcluir = viewModel::excluirProduto,
                        onAtualizarFoto = viewModel::atualizarFoto
                    )

                    Aba.VENDAS -> VendasScreen(
                        produtos = estado.todosProdutos,
                        onVender = viewModel::vender,
                        onGerarExemplos = viewModel::gerarPedidosExemplo
                    )

                    Aba.GERAR_NF -> GerarNfScreen(
                        pendentes = estado.pendentes,
                        faturados = estado.faturados,
                        onGerarNf = viewModel::gerarNf,
                        onGerarTodas = viewModel::gerarTodasNfs
                    )

                    Aba.NOTAS -> NotasLogScreen(
                        notas = estado.notas,
                        logs = estado.logs
                    )
                }
            }
        }
    }
}