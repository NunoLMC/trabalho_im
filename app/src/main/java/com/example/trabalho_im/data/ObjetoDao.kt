package com.example.trabalho_im.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Ficha 9 | Codelab 4 ---
// DAO (Data Access Object): mapeia funções Kotlin para queries SQL
// --- Ficha 10 | Codelab 5 ---
// Adiciona queries para ler, pesquisar e atualizar dados
@Dao
interface ObjetoDao {

    // Ficha 9: inserir novo objeto na base de dados
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserir(objeto: Objeto)

    // Ficha 10: atualizar um objeto já existente
    @Update
    suspend fun atualizar(objeto: Objeto)

    // Ficha 10: remover um objeto da base de dados
    @Delete
    suspend fun remover(objeto: Objeto)

    // Ficha 10: obter todos os objetos, do mais recente para o mais antigo
    @Query("SELECT * FROM objetos ORDER BY id DESC")
    fun getTodosObjetos(): Flow<List<Objeto>>

    // Ficha 10: pesquisar objetos pelo nome ou pela localização
    @Query("SELECT * FROM objetos WHERE nome LIKE :query OR localizacao LIKE :query ORDER BY id DESC")
    fun pesquisar(query: String): Flow<List<Objeto>>
}
