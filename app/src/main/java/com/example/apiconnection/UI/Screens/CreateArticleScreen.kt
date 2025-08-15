package com.example.apiconnection.UI.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.apiconnection.Network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.NumberFormat
import java.util.*

@Composable
fun CreateArticleScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()

    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.size > 4 || imageUris.size + uris.size > 4) {
            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Solo puedes seleccionar hasta 4 imágenes.") }
        } else {
            imageUris = imageUris + uris
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Crear nuevo artículo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    val clean = input.replace(",", "")
                    if (clean.matches(Regex("^\\d*(\\.\\d{0,2})?\$"))) {
                        price = if (clean.isNotEmpty()) {
                            try {
                                val parsed = clean.toDouble()
                                NumberFormat.getNumberInstance(Locale.US).apply {
                                    minimumFractionDigits = 0
                                    maximumFractionDigits = 2
                                }.format(parsed)
                            } catch (e: Exception) { clean }
                        } else ""
                    }
                },
                label = { Text("Precio") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Seleccionar Imágenes")
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                itemsIndexed(imageUris) { index, uri ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                imageUris = imageUris.toMutableList().also { it.removeAt(index) }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar imagen",
                                tint = Color.Gray
                            )
                        }

                        val painter = rememberImagePainter(uri)
                        Image(
                            painter = painter,
                            contentDescription = "Imagen seleccionada",
                            modifier = Modifier
                                .size(100.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("${imageUris.size} / 4 imágenes seleccionadas", style = MaterialTheme.typography.caption)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank() || price.isBlank() || imageUris.isEmpty()) {
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("Por favor completa todos los campos y selecciona al menos 1 imagen.") }
                        return@Button
                    }

                    scope.launch(Dispatchers.IO) {
                        try {
                            isLoading = true

                            val namePart = name.toRequestBody("text/plain".toMediaType())
                            val descriptionPart = description.toRequestBody("text/plain".toMediaType())
                            val cleanPrice = price.replace(",", "")
                            val pricePart = cleanPrice.toRequestBody("text/plain".toMediaType())

                            val contentResolver = context.contentResolver

                            val imageParts = imageUris.mapIndexed { index, uri ->
                                val inputStream = contentResolver.openInputStream(uri)
                                val tempFile = File.createTempFile("image_${index}_", ".jpg", context.cacheDir)
                                inputStream?.use { input ->
                                    tempFile.outputStream().use { output -> input.copyTo(output) }
                                }
                                val requestFile = tempFile.asRequestBody("image/*".toMediaType())
                                MultipartBody.Part.createFormData("images[$index]", tempFile.name, requestFile)
                            }

                            val response = RetrofitClient.api(context).createArticulo(
                                name = namePart,
                                description = descriptionPart,
                                price = pricePart,
                                images = imageParts
                            )

                            withContext(Dispatchers.Main) {
                                isLoading = false
                                if (response.isSuccessful) {
                                    scaffoldState.snackbarHostState.showSnackbar("Artículo creado correctamente.")
                                    delay(500L)
                                    navController.navigate("home") {
                                        popUpTo("create") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    scaffoldState.snackbarHostState.showSnackbar("Error al crear el artículo: ${response.code()}")
                                }
                            }

                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                scaffoldState.snackbarHostState.showSnackbar("Excepción: ${e.message}")
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Guardando..." else "Guardar")
            }
        }
    }
}
