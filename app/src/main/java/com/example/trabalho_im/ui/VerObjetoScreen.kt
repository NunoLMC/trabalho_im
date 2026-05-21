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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.trabalho_im.viewmodel.ObjetoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

// --- Ficha 10 | Codelab 5 ---
// Exibir e editar dados de um objeto
// --- Ficha 2 | Codelab 4 ---
// Mostrar imagens com a biblioteca Coil
// --- Ficha 3 ---
// Intents para câmara e Maps, pedido de permissões em tempo de execução
@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerObjetoScreen(
    navController: NavController,
    objetoId: Int,
    viewModel: ObjetoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ficha 10: obtém o objeto pelo ID a partir da lista do ViewModel
    val objetos by viewModel.objetos.collectAsState()
    val objeto = objetos.find { it.id == objetoId }

    if (objeto == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Ficha 6: estado editável dos campos — inicializado com os dados do objeto
    var nome by remember(objeto) { mutableStateOf(objeto.nome) }
    var localizacao by remember(objeto) { mutableStateOf(objeto.localizacao) }
    var localizacaoMapa by remember(objeto) { mutableStateOf(objeto.localizacaoMapa) }
    var fotoPath by remember(objeto) { mutableStateOf(objeto.fotoPath) }
    var mostrarFotoCompleta by remember { mutableStateOf(false) }

    // --- Câmara ---

    // pathPendente guarda o caminho do novo ficheiro enquanto a câmara está aberta
    // só é copiado para fotoPath se a câmara confirmar que guardou (guardou == true)
    var pathPendente by remember { mutableStateOf("") }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { guardou ->
        if (guardou && pathPendente.isNotBlank()) {
            fotoPath = pathPendente  // só atualiza quando a foto foi mesmo tirada
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
                val uri = Uri.parse(
                    "geo:${loc.latitude},${loc.longitude}?q=${Uri.encode(endereco)}"
                )
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
    }

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
                title = { Text("Ver Objeto", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // --- Nome ---
            Text("Nome")
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nome")
                }
            )

            // --- Localização descritiva ---
            // Ficha 10: campo livre para descrever onde o objeto está na divisão
            Text("Onde está o objeto?")
            OutlinedTextField(
                value = localizacao,
                onValueChange = { localizacao = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Ex: debaixo da mesa da sala") },
                trailingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "Editar localização")
                }
            )

            // --- Localização no mapa ---
            // Ficha 3: campo com morada, ícone abre Maps e atualiza com GPS atual
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

            // Ficha 3: abre o Maps com a morada guardada (se existir)
            if (localizacaoMapa.isNotBlank()) {
                Button(
                    onClick = {
                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(localizacaoMapa)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver no Google Maps")
                }
            }

            // --- Fotografia ---
            // Ficha 2: mostrar a foto com AsyncImage — clicável para ver em ecrã completo
            if (fotoPath.isNotBlank() && File(fotoPath).exists()) {
                AsyncImage(
                    model = File(fotoPath),
                    contentDescription = "Foto do objeto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { mostrarFotoCompleta = true },
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("Sem fotografia")
                    }
                }
            }

            // Ficha 3: abrir câmara para tirar nova foto
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Tirar foto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ficha 10: guardar as alterações
            Button(
                onClick = {
                    viewModel.atualizar(
                        objeto.copy(
                            nome = nome.trim(),
                            localizacao = localizacao.trim(),
                            localizacaoMapa = localizacaoMapa.trim(),
                            fotoPath = fotoPath
                        )
                    )
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar alterações")
            }

            // Ficha 10: remover o objeto
            Button(
                onClick = {
                    viewModel.remover(objeto)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Remover")
            }
        }
    }

    // Ficha 2: foto em ecrã completo — toca em qualquer sítio para fechar
    if (mostrarFotoCompleta && fotoPath.isNotBlank() && File(fotoPath).exists()) {
        Dialog(
            onDismissRequest = { mostrarFotoCompleta = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { mostrarFotoCompleta = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(fotoPath),
                    contentDescription = "Foto em ecrã completo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
