package com.example.trabalho_im.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.trabalho_im.viewmodel.ObjetoViewModel

// --- Ficha 4 | Codelab 4 (Pathway 1) ---
// Construção de interface com LazyColumn (lista) e animações
// --- Ficha 5 | Codelab 6 ---
// Caixa de pesquisa e layouts avançados
// --- Ficha 6 | Codelab 7 ---
// Botões com ícones e resposta a interações do utilizador
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ObjetoViewModel = viewModel()
) {
    // Ficha 8: recolher o estado do ViewModel com collectAsState
    val objetos by viewModel.objetos.collectAsState()
    val queryPesquisa by viewModel.queryPesquisa.collectAsState()

    // Ficha 10: filtrar localmente para refletir alterações imediatamente
    val listaParaMostrar = if (queryPesquisa.isBlank()) objetos
    else objetos.filter {
        it.nome.contains(queryPesquisa, ignoreCase = true) ||
                it.localizacao.contains(queryPesquisa, ignoreCase = true) ||
                it.localizacaoMapa.contains(queryPesquisa, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            // Ficha 7: TopAppBar com cores do tema da aplicação
            TopAppBar(
                title = { Text("Últimos Objetos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ficha 2: cabeçalho com texto formatado (bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Nome", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Localização", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp))
            }
            HorizontalDivider()

            // Ficha 4: LazyColumn para lista de objetos com animação de tamanho
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            ) {
                items(listaParaMostrar) { objeto ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = objeto.nome, modifier = Modifier.weight(1f))
                        Text(text = objeto.localizacao, modifier = Modifier.weight(1f))
                        // Ficha 6: botão com ícone para ver o objeto
                        IconButton(
                            onClick = { navController.navigate("ver_objeto/${objeto.id}") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Ver objeto",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }

            // Ficha 5: barra inferior com pesquisa e botão adicionar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ficha 5: campo de pesquisa
                OutlinedTextField(
                    value = queryPesquisa,
                    onValueChange = { viewModel.setPesquisa(it) },
                    placeholder = { Text("Pesquisar") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Pesquisar")
                    }
                )
                // Ficha 6: botão flutuante para ir ao ecrã de adicionar objeto
                FloatingActionButton(
                    onClick = { navController.navigate("novo_objeto") }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar objeto")
                }
            }
        }
    }
}
