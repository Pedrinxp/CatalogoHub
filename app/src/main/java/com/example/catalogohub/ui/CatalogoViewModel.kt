package com.example.catalogohub.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogohub.data.AppDatabase
import com.example.catalogohub.data.ArmazenamentoImagens
import com.example.catalogohub.data.CatalogoRepository
import com.example.catalogohub.data.GeradorDescricao
import com.example.catalogohub.data.GeradorDescricaoLocal
import com.example.catalogohub.data.LogSyncEntity
import com.example.catalogohub.data.NotaFiscalEntity
import com.example.catalogohub.data.PedidoEntity
import com.example.catalogohub.data.ProdutoEntity
import com.example.catalogohub.model.DashboardUi
import com.example.catalogohub.model.DescricaoIA
import com.example.catalogohub.model.Marketplace
import com.example.catalogohub.model.MesRef
import com.example.catalogohub.model.ResultadoNf
import com.example.catalogohub.model.ResultadoVenda
import com.example.catalogohub.model.ResumoUi
import com.example.catalogohub.model.StatusPedido
import com.example.catalogohub.model.comoNumeroNota
import com.example.catalogohub.model.comoNumeroPedido
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Pedido + foto do produto (para mostrar na aba Gerar NF). */
data class PedidoItemUi(
    val pedido: PedidoEntity,
    val foto: String?
)

/** Estado completo das telas. */
data class CatalogoUiState(
    val produtos: List<ProdutoEntity> = emptyList(),       // já filtrados pela busca
    val todosProdutos: List<ProdutoEntity> = emptyList(),  // sem filtro
    val busca: String = "",
    val resumo: ResumoUi = ResumoUi(),
    val notas: List<NotaFiscalEntity> = emptyList(),
    val logs: List<LogSyncEntity> = emptyList(),
    val pendentes: List<PedidoItemUi> = emptyList(),       // pedidos AGUARDANDO_NF
    val faturados: List<PedidoItemUi> = emptyList()        // pedidos FATURADO
)

/** Estado do botão "Gerar descrição com IA". */
data class IaState(
    val gerando: Boolean = false,
    val sugestao: DescricaoIA? = null
)

/** Converte "59,90" ou "59.90" em Double. */
private fun String.comoPreco(): Double? = trim().replace(',', '.').toDoubleOrNull()

class CatalogoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = CatalogoRepository(AppDatabase.getInstance(application))

    // Para usar uma API real, troque por GeradorDescricaoRemoto()
    private val gerador: GeradorDescricao = GeradorDescricaoLocal()

    private val _busca = MutableStateFlow("")
    private val _mes = MutableStateFlow(MesRef.atual())
    private val _ia = MutableStateFlow(IaState())
    val ia: StateFlow<IaState> = _ia.asStateFlow()

    // Mensagens pontuais para o Snackbar
    private val _mensagens = Channel<String>(Channel.BUFFERED)
    val mensagens = _mensagens.receiveAsFlow()

    val uiState: StateFlow<CatalogoUiState> = combine(
        repositorio.produtos,
        repositorio.notas,
        repositorio.logs,
        repositorio.pedidos,
        _busca
    ) { produtos, notas, logs, pedidos, busca ->
        val termo = busca.trim()
        val filtrados = if (termo.isEmpty()) {
            produtos
        } else {
            produtos.filter {
                it.nome.contains(termo, ignoreCase = true) || it.sku.contains(termo, ignoreCase = true)
            }
        }
        val fotos = produtos.associate { it.id to it.foto }
        val itens = pedidos.map { PedidoItemUi(it, fotos[it.produtoId]) }

        CatalogoUiState(
            produtos = filtrados,
            todosProdutos = produtos,
            busca = busca,
            resumo = ResumoUi(
                totalProdutos = produtos.size,
                totalUnidades = produtos.sumOf { it.estoque },
                pedidos = pedidos.size,
                valorVendido = pedidos.sumOf { it.total }
            ),
            notas = notas,
            logs = logs,
            pendentes = itens.filter { it.pedido.status == StatusPedido.AGUARDANDO_NF.name },
            faturados = itens.filter { it.pedido.status == StatusPedido.FATURADO.name }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogoUiState())

    /** Números do dashboard: mudam quando chegam pedidos novos ou quando o mês é trocado. */
    val dashboard: StateFlow<DashboardUi> = combine(
        repositorio.pedidos,
        _mes
    ) { pedidos, mes ->
        repositorio.calcularDashboard(pedidos, mes)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUi())

    init {
        viewModelScope.launch { repositorio.popularExemplosSeVazio() }
    }

    private suspend fun avisar(mensagem: String) {
        _mensagens.send(mensagem)
    }

    fun onBuscaChange(texto: String) {
        _busca.value = texto
    }

    /** Dashboard: -1 = mês anterior, +1 = próximo mês. */
    fun mudarMes(delta: Int) {
        _mes.value = _mes.value.deslocar(delta)
    }

    fun criarProduto(
        nome: String,
        sku: String,
        precoTexto: String,
        estoqueTexto: String,
        descricao: String,
        fotoUri: Uri?,
        canais: List<Marketplace>,
        onSucesso: () -> Unit
    ) {
        viewModelScope.launch {
            val nomeLimpo = nome.trim()
            val skuLimpo = sku.trim().uppercase()
            if (nomeLimpo.isEmpty()) { avisar("Informe o nome do produto."); return@launch }
            if (skuLimpo.isEmpty()) { avisar("Informe o SKU do produto."); return@launch }
            val preco = precoTexto.comoPreco()
            if (preco == null || preco <= 0.0) { avisar("Informe um preço válido."); return@launch }
            val estoque = estoqueTexto.trim().toIntOrNull()
            if (estoque == null || estoque < 0) { avisar("Informe um estoque válido."); return@launch }
            if (canais.isEmpty()) { avisar("Escolha ao menos um canal de venda."); return@launch }

            val caminhoFoto = fotoUri?.let {
                ArmazenamentoImagens.salvarCopia(getApplication<Application>(), it)
            }
            val erro = repositorio.criarProduto(
                nomeLimpo, skuLimpo, preco, estoque, descricao.trim(), caminhoFoto, canais
            )
            if (erro != null) {
                ArmazenamentoImagens.apagar(caminhoFoto)
                avisar(erro)
                return@launch
            }
            avisar("Produto \"$nomeLimpo\" cadastrado e publicado em ${canais.size} canal(is).")
            _ia.value = IaState()
            onSucesso()
        }
    }

    fun editarProduto(
        id: Long,
        nome: String,
        precoTexto: String,
        descricao: String,
        fotoUri: Uri?,
        onSucesso: () -> Unit
    ) {
        viewModelScope.launch {
            val nomeLimpo = nome.trim()
            if (nomeLimpo.isEmpty()) { avisar("Informe o nome do produto."); return@launch }
            val preco = precoTexto.comoPreco()
            if (preco == null || preco <= 0.0) { avisar("Informe um preço válido."); return@launch }

            val caminhoFoto = fotoUri?.let {
                ArmazenamentoImagens.salvarCopia(getApplication<Application>(), it)
            }
            val erro = repositorio.editarProduto(id, nomeLimpo, preco, descricao.trim(), caminhoFoto)
            if (erro != null) {
                avisar(erro)
                return@launch
            }
            avisar("Produto atualizado e sincronizado nos canais.")
            _ia.value = IaState()
            onSucesso()
        }
    }

    fun atualizarFoto(id: Long, uri: Uri) {
        viewModelScope.launch {
            val caminho = ArmazenamentoImagens.salvarCopia(getApplication<Application>(), uri)
            if (caminho == null) {
                avisar("Não foi possível carregar a imagem.")
                return@launch
            }
            repositorio.atualizarFoto(id, caminho)
            avisar("Foto atualizada.")
        }
    }

    fun excluirProduto(produto: ProdutoEntity) {
        viewModelScope.launch {
            repositorio.excluirProduto(produto)
            avisar("Produto \"${produto.nome}\" excluído.")
        }
    }

    /** Venda: baixa o estoque e cria um pedido aguardando NF. */
    fun vender(produtoId: Long, canal: Marketplace, quantidade: Int) {
        viewModelScope.launch {
            when (val resultado = repositorio.vender(produtoId, canal, quantidade)) {
                is ResultadoVenda.Sucesso -> avisar(
                    "Venda em ${canal.nome} registrada! Pedido ${resultado.numeroPedido.comoNumeroPedido()} " +
                            "aguardando NF e estoque sincronizado."
                )
                is ResultadoVenda.Erro -> avisar(resultado.mensagem)
            }
        }
    }

    /** Gera a NF de um pedido pendente. */
    fun gerarNf(pedidoId: Long) {
        viewModelScope.launch {
            when (val resultado = repositorio.gerarNf(pedidoId)) {
                is ResultadoNf.Sucesso -> avisar("${resultado.numeroNota.comoNumeroNota()} gerada com sucesso!")
                is ResultadoNf.Erro -> avisar(resultado.mensagem)
            }
        }
    }

    /** Gera a NF de todos os pedidos pendentes. */
    fun gerarTodasNfs() {
        viewModelScope.launch {
            val quantidade = repositorio.gerarTodasNfs()
            if (quantidade == 0) {
                avisar("Não há pedidos pendentes.")
            } else {
                avisar("$quantidade nota(s) fiscal(is) gerada(s) com sucesso!")
            }
        }
    }

    /** Cria pedidos de exemplo no mês atual para preencher os gráficos. */
    fun gerarPedidosExemplo() {
        viewModelScope.launch {
            val quantidade = repositorio.gerarPedidosExemplo()
            if (quantidade == 0) {
                avisar("Cadastre um produto antes de gerar pedidos de exemplo.")
            } else {
                avisar("$quantidade pedidos de exemplo criados neste mês.")
            }
        }
    }

    fun gerarDescricao(nomeProduto: String) {
        if (nomeProduto.isBlank()) return
        viewModelScope.launch {
            _ia.value = IaState(gerando = true)
            val sugestao = try {
                gerador.gerar(nomeProduto)
            } catch (e: Exception) {
                null
            }
            _ia.value = IaState(sugestao = sugestao)
            if (sugestao == null) avisar("Não foi possível gerar a descrição agora.")
        }
    }

    fun limparSugestao() {
        _ia.value = IaState()
    }
}