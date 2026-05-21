package com.example.trabalho_im.data

// --- Ficha 9 | Codelab 4 ---
// Classe de dados que representa um objeto guardado pelo utilizador
// (Numa app com Room completo, teria @Entity, @PrimaryKey, etc.)
data class Objeto(
    val id: Int = 0,
    val nome: String,
    val localizacao: String,        // descrição livre: "debaixo da mesa da sala"
    val localizacaoMapa: String = "", // endereço do Maps: "Rua X, Lisboa"
    val fotoPath: String = ""
)
