package com.example.catalogohub.data

import androidx.room.withTransaction
import com.example.catalogohub.model.DashboardUi
import com.example.catalogohub.model.LIMITE_ESTOQUE_BAIXO
import com.example.catalogohub.model.Marketplace
import com.example.catalogohub.model.MesRef
import com.example.catalogohub.model.ResultadoNf
import com.example.catalogohub.model.ResultadoVenda
import com.example.catalogohub.model.StatusPedido
import com.example.catalogohub.model.TipoLog
import com.example.catalogohub.model.TotalCanal
import com.example.catalogohub.model.TotalDia
import com.example.catalogohub.model.comoMoeda
import com.example.catalogohub.model.comoNumeroNota
import com.example.catalogohub.model.comoNumeroPedido
import com.example.catalogohub.model.paraMarketplaces
import com.example.catalogohub.model.paraTexto
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlin.random.Random

/** Pedido de exemplo ainda não gravado (uso interno do gerador de dados de teste). */
private data class AmostraPedido(
    val produto: ProdutoEntity,
    val canal: Marketplace,
    val quantidade: Int,
    val data: Long
)

/**
 * Concentra TODA a regra de negócio de dados:
 * cadastro, estoque único sincronizado, pedidos, nota fiscal, log e agregação do dashboard.
 */
class CatalogoRepository(private val db: AppDatabase) {

    private val produtoDao = db.produtoDao()
    private val notaDao = db.notaFiscalDao()
    private val logDao = db.logSyncDao()
    private val pedidoDao = db.pedidoDao()

    val produtos: Flow<List<ProdutoEntity>> = produtoDao.observarTodos()
    val notas: Flow<List<NotaFiscalEntity>> = notaDao.observarTodas()
    val logs: Flow<List<LogSyncEntity>> = logDao.observarTodos()
    val pedidos: Flow<List<PedidoEntity>> = pedidoDao.observarTodos()

    private suspend fun registrarLog(
        mensagem: String,
        tipo: TipoLog,
        data: Long = System.currentTimeMillis()
    ) {
        logDao.inserir(LogSyncEntity(mensagem = mensagem, tipo = tipo.name, data = data))
    }

    /** Cria produtos de exemplo na primeira abertura do app. */
    suspend fun popularExemplosSeVazio() {
        if (produtoDao.contar() > 0) return

        val todos = Marketplace.entries.toList()
        val exemplos = listOf(
            ProdutoEntity(
                nome = "Camiseta Básica Algodão", sku = "CAM-001", preco = 59.90, estoque = 40,
                foto = "res:camiseta", canais = todos.paraTexto()
            ),
            ProdutoEntity(
                nome = "Fone Bluetooth", sku = "FON-002", preco = 149.90, estoque = 4,
                foto = "res:fone_bluetooth",
                canais = listOf(Marketplace.MERCADO_LIVRE, Marketplace.MAGALU, Marketplace.SHOPEE, Marketplace.TIKTOK_SHOP).paraTexto()
            ),
            ProdutoEntity(
                nome = "Caneca Térmica 500ml", sku = "CAN-003", preco = 39.90, estoque = 25,
                foto = "res:caneca",
                canais = listOf(Marketplace.MERCADO_LIVRE, Marketplace.SHOPEE, Marketplace.LOJA_PROPRIA).paraTexto()
            ),
            ProdutoEntity(
                nome = "Mochila Executiva", sku = "MOC-004", preco = 189.90, estoque = 12,
                foto = "res:mochila",
                canais = listOf(Marketplace.MAGALU, Marketplace.LOJA_PROPRIA).paraTexto()
            ),
            ProdutoEntity(
                nome = "Tênis Esportivo", sku = "TEN-005", preco = 229.90, estoque = 5,
                foto = "res:tenis", canais = todos.paraTexto()
            )
        )
        exemplos.forEach { produtoDao.inserir(it) }
        registrarLog("Catálogo inicial carregado com ${exemplos.size} produtos de exemplo.", TipoLog.INFO)
    }

    /** Cadastra um produto e "publica" nos canais escolhidos. Devolve mensagem de erro ou null. */
    suspend fun criarProduto(
        nome: String,
        sku: String,
        preco: Double,
        estoque: Int,
        descricao: String,
        foto: String?,
        canais: List<Marketplace>
    ): String? {
        if (produtoDao.contarPorSku(sku, 0) > 0) return "Já existe um produto com o SKU $sku."

        produtoDao.inserir(
            ProdutoEntity(
                nome = nome, sku = sku, preco = preco, estoque = estoque,
                foto = foto, descricao = descricao, canais = canais.paraTexto()
            )
        )
        canais.forEach {
            registrarLog("Produto \"$nome\" publicado em ${it.nome} (estoque: $estoque un.).", TipoLog.SYNC)
        }
        return null
    }

    /** Edita nome, preço, descrição e (opcionalmente) a foto. Devolve mensagem de erro ou null. */
    suspend fun editarProduto(
        id: Long,
        nome: String,
        preco: Double,
        descricao: String,
        novaFoto: String?
    ): String? {
        val atual = produtoDao.buscarPorId(id) ?: return "Produto não encontrado."
        val fotoFinal = novaFoto ?: atual.foto

        produtoDao.atualizar(atual.copy(nome = nome, preco = preco, descricao = descricao, foto = fotoFinal))
        if (novaFoto != null && novaFoto != atual.foto) ArmazenamentoImagens.apagar(atual.foto)

        val canais = atual.canais.paraMarketplaces().joinToString(", ") { it.nome }
        registrarLog("Produto \"$nome\" atualizado (${preco.comoMoeda()}) e sincronizado em: $canais.", TipoLog.SYNC)
        return null
    }

    suspend fun atualizarFoto(id: Long, novaFoto: String) {
        val atual = produtoDao.buscarPorId(id) ?: return
        produtoDao.atualizar(atual.copy(foto = novaFoto))
        ArmazenamentoImagens.apagar(atual.foto)
        registrarLog("Foto de \"${atual.nome}\" atualizada em todos os canais.", TipoLog.SYNC)
    }

    suspend fun excluirProduto(produto: ProdutoEntity) {
        produtoDao.excluir(produto)
        ArmazenamentoImagens.apagar(produto.foto)
        registrarLog("Produto \"${produto.nome}\" removido de todos os canais.", TipoLog.SYNC)
    }

    /**
     * Venda em um canal: baixa o estoque ÚNICO (vale para todos os canais),
     * cria um PEDIDO "Aguardando NF" (com o canal de origem) e registra o log.
     * A nota fiscal NÃO é emitida aqui: isso é feito na aba "Gerar NF".
     * Tudo em uma única transação.
     */
    suspend fun vender(produtoId: Long, canal: Marketplace, quantidade: Int): ResultadoVenda =
        db.withTransaction {
            val produto = produtoDao.buscarPorId(produtoId)
                ?: return@withTransaction ResultadoVenda.Erro("Produto não encontrado.")

            if (quantidade <= 0) {
                return@withTransaction ResultadoVenda.Erro("Quantidade inválida.")
            }
            val canaisDoProduto = produto.canais.paraMarketplaces()
            if (canal !in canaisDoProduto) {
                return@withTransaction ResultadoVenda.Erro("O produto não está publicado em ${canal.nome}.")
            }
            if (produto.estoque < quantidade) {
                return@withTransaction ResultadoVenda.Erro("Estoque insuficiente: restam ${produto.estoque} un.")
            }

            val agora = System.currentTimeMillis()
            val novoEstoque = produto.estoque - quantidade
            val total = produto.preco * quantidade
            val numeroPedido = (pedidoDao.maiorNumero() ?: 0) + 1

            produtoDao.atualizar(produto.copy(estoque = novoEstoque))

            pedidoDao.inserir(
                PedidoEntity(
                    numeroPedido = numeroPedido,
                    produtoId = produto.id,
                    produtoNome = produto.nome,
                    quantidade = quantidade,
                    total = total,
                    canal = canal.name,
                    data = agora,
                    status = StatusPedido.AGUARDANDO_NF.name,
                    numeroNota = null
                )
            )

            registrarLog("Venda de ${quantidade}x \"${produto.nome}\" em ${canal.nome}.", TipoLog.VENDA, agora)
            canaisDoProduto.forEach {
                registrarLog("Estoque sincronizado em ${it.nome}: $novoEstoque un.", TipoLog.SYNC, agora)
            }
            registrarLog(
                "Pedido ${numeroPedido.comoNumeroPedido()} criado via ${canal.nome} (${total.comoMoeda()}), aguardando NF.",
                TipoLog.PEDIDO,
                agora
            )

            if (novoEstoque <= LIMITE_ESTOQUE_BAIXO) {
                registrarLog("Alerta: \"${produto.nome}\" com estoque baixo ($novoEstoque un.).", TipoLog.ESTOQUE, agora)
            }

            ResultadoVenda.Sucesso(numeroPedido)
        }

    /** Emite a NF de um pedido e o marca como FATURADO. Deve ser chamada dentro de uma transação. */
    private suspend fun emitirNota(pedido: PedidoEntity, data: Long): Int {
        val numero = (notaDao.maiorNumero() ?: 0) + 1
        notaDao.inserir(
            NotaFiscalEntity(
                numero = numero,
                produtoId = pedido.produtoId,
                produtoNome = pedido.produtoNome,
                quantidade = pedido.quantidade,
                total = pedido.total,
                canal = pedido.canal,
                data = data
            )
        )
        pedidoDao.atualizar(pedido.copy(status = StatusPedido.FATURADO.name, numeroNota = numero))
        registrarLog(
            "Nota fiscal simulada ${numero.comoNumeroNota()} emitida para o pedido " +
                    "${pedido.numeroPedido.comoNumeroPedido()} (${pedido.total.comoMoeda()}).",
            TipoLog.NOTA,
            data
        )
        return numero
    }

    /** Gera a NF de UM pedido pendente (em transação). */
    suspend fun gerarNf(pedidoId: Long): ResultadoNf =
        db.withTransaction {
            val pedido = pedidoDao.buscarPorId(pedidoId)
                ?: return@withTransaction ResultadoNf.Erro("Pedido não encontrado.")
            if (pedido.status != StatusPedido.AGUARDANDO_NF.name) {
                return@withTransaction ResultadoNf.Erro("Este pedido já foi faturado.")
            }
            ResultadoNf.Sucesso(emitirNota(pedido, System.currentTimeMillis()))
        }

    /** Gera a NF de TODOS os pedidos pendentes, em uma única transação. Devolve quantas foram geradas. */
    suspend fun gerarTodasNfs(): Int =
        db.withTransaction {
            val pendentes = pedidoDao.buscarPorStatus(StatusPedido.AGUARDANDO_NF.name)
            val agora = System.currentTimeMillis()
            pendentes.forEach { emitirNota(it, agora) }
            pendentes.size
        }

    /**
     * Cria pedidos de exemplo com datas espalhadas pelo mês atual (do dia 1 até hoje).
     * Não mexe no estoque. Cerca de 1/3 já nasce faturado, com a nota correspondente.
     * Devolve quantos pedidos foram criados.
     */
    suspend fun gerarPedidosExemplo(): Int =
        db.withTransaction {
            val disponiveis = produtoDao.listar().filter { it.canais.paraMarketplaces().isNotEmpty() }
            if (disponiveis.isEmpty()) return@withTransaction 0

            val hoje = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val amostras = List(24) {
                val produto = disponiveis.random()
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, Random.nextInt(1, hoje + 1))
                    set(Calendar.HOUR_OF_DAY, Random.nextInt(8, 22))
                    set(Calendar.MINUTE, Random.nextInt(0, 60))
                }
                AmostraPedido(
                    produto = produto,
                    canal = produto.canais.paraMarketplaces().random(),
                    quantidade = Random.nextInt(1, 4),
                    data = cal.timeInMillis
                )
            }.sortedBy { it.data }

            var numero = pedidoDao.maiorNumero() ?: 0
            amostras.forEach { a ->
                numero++
                val pedido = PedidoEntity(
                    numeroPedido = numero,
                    produtoId = a.produto.id,
                    produtoNome = a.produto.nome,
                    quantidade = a.quantidade,
                    total = a.produto.preco * a.quantidade,
                    canal = a.canal.name,
                    data = a.data,
                    status = StatusPedido.AGUARDANDO_NF.name,
                    numeroNota = null
                )
                val id = pedidoDao.inserir(pedido)
                if (Random.nextInt(3) == 0) emitirNota(pedido.copy(id = id), a.data)
            }

            registrarLog("${amostras.size} pedidos de exemplo gerados (estoque não alterado).", TipoLog.INFO)
            amostras.size
        }

    /**
     * Agrega os pedidos do mês escolhido (qualquer status) para o dashboard.
     * Função pura: recebe a lista e devolve os números prontos para a tela.
     */
    fun calcularDashboard(pedidos: List<PedidoEntity>, mes: MesRef): DashboardUi {
        val cal = Calendar.getInstance()

        val doMes = pedidos.filter {
            cal.timeInMillis = it.data
            cal.get(Calendar.YEAR) == mes.ano && cal.get(Calendar.MONTH) + 1 == mes.mes
        }

        val faturamento = doMes.sumOf { it.total }

        // Faturamento por dia
        val referencia = Calendar.getInstance().apply {
            clear()
            set(mes.ano, mes.mes - 1, 1)
        }
        val diasNoMes = referencia.getActualMaximum(Calendar.DAY_OF_MONTH)
        val somaPorDia = DoubleArray(diasNoMes + 1)
        doMes.forEach {
            cal.timeInMillis = it.data
            somaPorDia[cal.get(Calendar.DAY_OF_MONTH)] += it.total
        }
        val porDia = (1..diasNoMes).map { TotalDia(it, somaPorDia[it]) }
        val melhorDia = porDia.maxByOrNull { it.faturamento }?.takeIf { it.faturamento > 0.0 }

        // Totais por canal (todos os canais aparecem, mesmo com zero)
        val porCanal = Marketplace.entries.map { canal ->
            val doCanal = doMes.filter { it.canal == canal.name }
            val valor = doCanal.sumOf { it.total }
            TotalCanal(
                canal = canal,
                faturamento = valor,
                pedidos = doCanal.size,
                percentual = if (faturamento > 0.0) (valor / faturamento).toFloat() else 0f
            )
        }.sortedByDescending { it.faturamento }

        return DashboardUi(
            mes = mes,
            faturamento = faturamento,
            totalPedidos = doMes.size,
            ticketMedio = if (doMes.isNotEmpty()) faturamento / doMes.size else 0.0,
            aguardandoNf = doMes.count { it.status == StatusPedido.AGUARDANDO_NF.name },
            porCanal = porCanal,
            porDia = porDia,
            melhorDia = melhorDia
        )
    }
}