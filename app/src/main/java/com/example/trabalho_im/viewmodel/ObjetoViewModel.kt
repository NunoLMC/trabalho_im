package com.example.trabalho_im.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalho_im.data.Objeto
import com.example.trabalho_im.data.ObjetoRepositorio
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- Ficha 10 | Codelab 5 ---
// ViewModel: gere os dados e expõe-os à UI via StateFlow
// --- Ficha 8 | Codelab 5 ---
// Usa StateFlow e coroutines para gerir o estado da interface
class ObjetoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = ObjetoRepositorio(application)

    // Ficha 10: todos os objetos para acesso direto (ex: encontrar por ID)
    val objetos: StateFlow<List<Objeto>> = repositorio.objetos

    // Ficha 8: estado da pesquisa - atualizado quando o utilizador escreve
    private val _queryPesquisa = MutableStateFlow("")
    val queryPesquisa: StateFlow<String> = _queryPesquisa.asStateFlow()

    // Ficha 10: lista filtrada com base no texto de pesquisa
    val objetosFiltrados: StateFlow<List<Objeto>> = combine(
        repositorio.objetos,
        _queryPesquisa
    ) { lista, query ->
        if (query.isBlank()) lista
        else repositorio.pesquisar(query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun setPesquisa(query: String) {
        _queryPesquisa.value = query
    }

    // Ficha 9: inserir novo objeto
    fun inserir(objeto: Objeto) {
        viewModelScope.launch {
            repositorio.inserir(objeto)
        }
    }

    // Ficha 10: guardar alterações a um objeto existente
    fun atualizar(objeto: Objeto) {
        viewModelScope.launch {
            repositorio.atualizar(objeto)
        }
    }

    // Ficha 10: apagar um objeto
    fun remover(objeto: Objeto) {
        viewModelScope.launch {
            repositorio.remover(objeto)
        }
    }
}
