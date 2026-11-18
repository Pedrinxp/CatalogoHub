package com.example.catalogohub.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
object ArmazenamentoImagens {
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
    fun apagar(caminho: String?) {
        if (caminho.isNullOrBlank() || caminho.startsWith("res:")) return
        try {
            File(caminho).delete()
        } catch (e: Exception) {
        }
    }
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
