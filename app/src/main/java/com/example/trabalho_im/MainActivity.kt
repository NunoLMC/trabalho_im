package com.example.trabalho_im

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trabalho_im.ui.HomeScreen
import com.example.trabalho_im.ui.NovoObjetoScreen
import com.example.trabalho_im.ui.VerObjetoScreen
import com.example.trabalho_im.ui.theme.Trabalho_imTheme
import com.example.trabalho_im.viewmodel.ObjetoViewModel

// --- Ficha 1 | Codelab 3 ---
// Ponto de entrada principal da aplicação Android
// --- Ficha 3 | Codelab: Ciclo de vida da atividade ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Ficha 7: aplica o tema Material3 a toda a aplicação
            Trabalho_imTheme {
                val navController = rememberNavController()

                // Ficha 10: ViewModel criado uma única vez aqui, partilhado por todos os ecrãs
                // Assim todos os ecrãs trabalham com os mesmos dados em tempo real
                val viewModel: ObjetoViewModel = viewModel()

                // Ficha 3: NavHost define as rotas de navegação entre ecrãs
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    composable("home") {
                        HomeScreen(navController = navController, viewModel = viewModel)
                    }
                    composable("novo_objeto") {
                        NovoObjetoScreen(navController = navController, viewModel = viewModel)
                    }
                    // Ficha 3: argumento na rota para saber qual objeto abrir
                    composable("ver_objeto/{objetoId}") { backStackEntry ->
                        val objetoId = backStackEntry.arguments
                            ?.getString("objetoId")?.toIntOrNull() ?: 0
                        VerObjetoScreen(
                            navController = navController,
                            objetoId = objetoId,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}