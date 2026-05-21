package com.example.trabalho_im.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

// --- Ficha 9 | Codelab 4 ---
// Repositório: guarda e lê os objetos num ficheiro JSON local
// (Equivalente ao que o Room faria com SQLite, mas sem plugin extra)
class ObjetoRepositorio(private val context: Context) {

    private val ficheiro = "objetos.json"

    private val _objetos = MutableStateFlow<List<Objeto>>(emptyList())
    val objetos: StateFlow<List<Objeto>> = _objetos.asStateFlow()

    init {
        _objetos.value = lerFicheiro()
    }

    // Ficha 9: guardar lista no ficheiro JSON
    private fun guardarFicheiro(lista: List<Objeto>) {
        val array = JSONArray()
        for (obj in lista) {
            val item = JSONObject()
            item.put("id", obj.id)
            item.put("nome", obj.nome)
            item.put("localizacao", obj.localizacao)
            item.put("localizacaoMapa", obj.localizacaoMapa)
            item.put("fotoPath", obj.fotoPath)
            array.put(item)
        }
        context.openFileOutput(ficheiro, Context.MODE_PRIVATE).use {
            it.write(array.toString().toByteArray())
        }
        _objetos.value = lista
    }

    // Ficha 9: ler lista do ficheiro JSON
    private fun lerFicheiro(): List<Objeto> {
        return try {
            val texto = context.openFileInput(ficheiro).bufferedReader().readText()
            val array = JSONArray(texto)
            val lista = mutableListOf<Objeto>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                lista.add(
                    Objeto(
                        id = item.getInt("id"),
                        nome = item.getString("nome"),
                        localizacao = item.getString("localizacao"),
                        localizacaoMapa = item.optString("localizacaoMapa", ""),
                        fotoPath = item.optString("fotoPath", "")
                    )
                )
            }
            lista
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Ficha 9: inserir novo objeto com id gerado automaticamente
    fun inserir(objeto: Objeto) {
        val lista = _objetos.value.toMutableList()
        val novoId = if (lista.isEmpty()) 1 else lista.maxOf { it.id } + 1
        lista.add(0, objeto.copy(id = novoId))
        guardarFicheiro(lista)
    }

    // Ficha 10: atualizar objeto existente
    fun atualizar(objeto: Objeto) {
        val lista = _objetos.value.map { if (it.id == objeto.id) objeto else it }
        guardarFicheiro(lista)
    }

    // Ficha 10: remover objeto
    fun remover(objeto: Objeto) {
        val lista = _objetos.value.filter { it.id != objeto.id }
        guardarFicheiro(lista)
    }

    // Ficha 10: pesquisar por nome ou localização
    fun pesquisar(query: String): List<Objeto> {
        val q = query.lowercase()
        return _objetos.value.filter {
            it.nome.lowercase().contains(q) ||
            it.localizacao.lowercase().contains(q) ||
            it.localizacaoMapa.lowercase().contains(q)
        }
    }
}
