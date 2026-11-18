package com.example.catalogohub.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Cuida das fotos dos produtos.
 * - Fotos da galeria são COPIADAS para a pasta interna do app, para não se perderem.
 * - Fotos de exemplo ficam em res/drawable e são referenciadas por "res:nome".
 */
object ArmazenamentoImagens {

    /** Copia a imagem escolhida na galeria para o armazenamento interno e devolve o caminho. */
    suspend fun salvarCopia(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val pasta = File(context.filesDir, "fotos").apply { mkdirs() }
            val destino = File(pasta, "foto_${System.currentTimeMillis()}.jpg")
            val entrada = context.contentResolver.openInputStream(uri) ?: return@withContext null
            entrada.use { origem ->
                destino.outputStream().use { saida -> origem.copyTo(saida) }
            }
            destino.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /** Apaga o arquivo da foto (ignora as fotos de exemplo "res:"). */
    fun apagar(caminho: String?) {
        if (caminho.isNullOrBlank() || caminho.startsWith("res:")) return
        try {
            File(caminho).delete()
        } catch (e: Exception) {
            // Sem problema se não conseguir apagar
        }
    }

    /**
     * Devolve o objeto que o Coil sabe carregar (Int do drawable ou File),
     * ou null quando não há foto. Se o drawable de exemplo não existir, devolve null
     * e a tela mostra o placeholder com a inicial.
     */
    @SuppressLint("DiscouragedApi")
    fun resolverModelo(context: Context, foto: String?): Any? {
        if (foto.isNullOrBlank()) return null
        return if (foto.startsWith("res:")) {
            val nome = foto.removePrefix("res:")
            val id = context.resources.getIdentifier(nome, "drawable", context.packageName)
            if (id != 0) id else null
        } else {
            File(foto).takeIf { it.exists() }
        }
    }
}