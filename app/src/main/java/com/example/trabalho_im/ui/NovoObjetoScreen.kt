package com.example.trabalho_im.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import coil.compose.AsyncImage
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.trabalho_im.data.Objeto
import com.example.trabalho_im.viewmodel.ObjetoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

// --- Ficha 6 | Codelab 7 ---
// Interface com campos de texto e botões, estado com remember + mutableStateOf
// --- Ficha 3 ---
// Intents para câmara e Maps, pedido de permissões em tempo de execução
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoObjetoScreen(
    navController: NavController,
    viewModel: ObjetoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ficha 6: estado dos campos — guardam o que o utilizador escreve
    var nome by remember { mutableStateOf("") }
    var localizacao by remember { mutableStateOf("") }       // descrição livre
    var localizacaoMapa by remember { mutableStateOf("") }   // endereço do Maps
    var fotoPath by remember { mutableStateOf("") }

    // --- Câmara ---

    // pathPendente guarda o caminho do ficheiro enquanto a câmara está aberta
    var pathPendente by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { guardou ->
        if (guardou && pathPendente.isNotBlank()) {
            fotoPath = pathPendente  // só atualiza se a foto foi mesmo tirada
        }
        pathPendente = ""
    }

    fun criarUriFoto(): Uri {
        val ficheiro = File(context.filesDir, "obj_${System.currentTimeMillis()}.jpg")
        pathPendente = ficheiro.absolutePath  // guarda o caminho mas NÃO toca em fotoPath
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", ficheiro)
    }

    val permissaoCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida -> if (concedida) cameraLauncher.launch(criarUriFoto()) }

    // --- Localização Maps ---

    // Ficha 3: obtém GPS, converte para morada com Geocoder e abre o Maps
    fun obterLocalizacaoMapa() {
        scope.launch {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (loc != null) {
                // Geocoder converte coordenadas em morada legível (requer rede)
                val endereco = withContext(Dispatchers.IO) {
                    try {
                        @Suppress("DEPRECATION")
                        val lista = Geocoder(context, Locale.getDefault())
                            .getFromLocation(loc.latitude, loc.longitude, 1)
                        if (!lista.isNullOrEmpty()) lista[0].getAddressLine(0)
                        else String.format(Locale.US, "%.5f, %.5f", loc.latitude, loc.longitude)
                    } catch (e: Exception) {
                        String.format(Locale.US, "%.5f, %.5f", loc.latitude, loc.longitude)
                    }
                }
                localizacaoMapa = endereco
                // abre o Maps nessa posição
                val uri = Uri.parse(
                    "geo:${loc.latitude},${loc.longitude}?q=${Uri.encode(endereco)}"
                )
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }

    // Ficha 3: launcher para pedir permissão de localização em tempo de execução
    val permissaoLocalizacaoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val concedida = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedida) obterLocalizacaoMapa()
    }

    fun clicarMaps() {
        val temPermissao = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (temPermissao) obterLocalizacaoMapa()
        else permissaoLocalizacaoLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo Objeto", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Nome ---
            Text("Nome")
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Localização descritiva ---
            // Ficha 6: campo livre para descrever onde o objeto está na divisão
            Text("Onde está o objeto?")
            OutlinedTextField(
                value = localizacao,
                onValueChange = { localizacao = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ex: debaixo da mesa da sala") }
            )

            // --- Localização no mapa ---
            // Ficha 3: campo preenchido via GPS + Geocoder, com botão para abrir Maps
            Text("Morada / Localização no Mapa")
            OutlinedTextField(
                value = localizacaoMapa,
                onValueChange = { localizacaoMapa = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Toca no ícone para usar o GPS") },
                trailingIcon = {
                    IconButton(onClick = { clicarMaps() }) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Obter localização GPS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )

            // --- Fotografia ---
            Text("Tirar uma fotografia")
            Button(
                onClick = {
                    val perm = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    )
                    if (perm == PackageManager.PERMISSION_GRANTED)
                        cameraLauncher.launch(criarUriFoto())
                    else
                        permissaoCameraLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Abrir câmara",
                    modifier = Modifier.size(36.dp)
                )
            }

            // Ficha 2: pré-visualização da foto tirada, usando Coil (AsyncImage)
            if (fotoPath.isNotBlank() && File(fotoPath).exists()) {
                AsyncImage(
                    model = File(fotoPath),
                    contentDescription = "Pré-visualização da foto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
                Text("Fotografia guardada", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.weight(1f))

            // Ficha 9: guardar o objeto — exige nome e pelo menos um dos campos de localização
            Button(
                onClick = {
                    if (nome.isNotBlank() && (localizacao.isNotBlank() || localizacaoMapa.isNotBlank())) {
                        viewModel.inserir(
                            Objeto(
                                nome = nome.trim(),
                                localizacao = localizacao.trim(),
                                localizacaoMapa = localizacaoMapa.trim(),
                                fotoPath = fotoPath
                            )
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nome.isNotBlank() && (localizacao.isNotBlank() || localizacaoMapa.isNotBlank())
            ) {
                Text("Adicionar")
            }
        }
    }
}
