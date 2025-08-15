package com.example.apiconnection.UI.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
fun EditArticleScreen(navController: NavController, articleId: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }      // nuevas imágenes
    var existingImageUrls by remember { mutableStateOf<List<String>>(emptyList()) } // actuales en server
    var deletedImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }  // urls a borrar

    // Cargar datos del artículo
    LaunchedEffect(articleId) {
        try {
            val response = RetrofitClient.api(context).getArticulos()
            val article = response.data.find { it.id == articleId }

            if (article != null) {
                name = article.name
                description = article.description
                price = article.price.toString()
                existingImageUrls = article.images.map { it.url }
            } else {
                scaffoldState.snackbarHostState.showSnackbar("Artículo no encontrado")
                navController.popBackStack()
            }
        } catch (e: Exception) {
            scaffoldState.snackbarHostState.showSnackbar("Error al cargar el artículo")
            navController.popBackStack()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if ((existingImageUrls.size + imageUris.size + uris.size) > 4) {
            scope.launch { scaffoldState.snackbarHostState.showSnackbar("Máximo 4 imágenes en total.") }
        } else {
            imageUris = imageUris + uris
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Editar artículo", color = Color.White) },
                backgroundColor = Color(0xFF1976D2),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                }
            )
        },
        backgroundColor = Color(0xFFF5F5F5)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1976D2),
                    cursorColor = Color(0xFF1976D2)
                )
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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1976D2),
                    cursorColor = Color(0xFF1976D2)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, shape = MaterialTheme.shapes.medium),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1976D2),
                    cursorColor = Color(0xFF1976D2)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1976D2)),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Agregar nuevas imágenes", color = Color.White) }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow {
                // imágenes existentes (con opción de quitar)
                items(existingImageUrls) { url ->
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        Image(
                            painter = rememberImagePainter(url),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                        IconButton(
                            onClick = {
                                existingImageUrls = existingImageUrls - url
                                deletedImageUrls = deletedImageUrls + url
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) { Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red) }
                    }
                }

                // nuevas imágenes seleccionadas
                items(imageUris) { uri ->
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        Image(
                            painter = rememberImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                        IconButton(
                            onClick = { imageUris = imageUris - uri },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) { Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.Red) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("${existingImageUrls.size + imageUris.size} / 4 imágenes", style = MaterialTheme.typography.caption, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank() || price.isBlank()) {
                        scope.launch { scaffoldState.snackbarHostState.showSnackbar("Completa todos los campos.") }
                        return@Button
                    }

                    scope.launch(Dispatchers.IO) {
                        try {
                            isLoading = true

                            val namePart = name.toRequestBody("text/plain".toMediaType())
                            val descriptionPart = description.toRequestBody("text/plain".toMediaType())
                            val pricePart = price.replace(",", "").toRequestBody("text/plain".toMediaType())
                            val methodPut = "PUT".toRequestBody("text/plain".toMediaType())

                            val contentResolver = context.contentResolver
                            val imageParts = imageUris.mapIndexed { index, uri ->
                                val inputStream = contentResolver.openInputStream(uri)
                                val tempFile = File.createTempFile("image_${index}_", ".jpg", context.cacheDir)
                                inputStream?.use { input ->
                                    tempFile.outputStream().use { output -> input.copyTo(output) }
                                }
                                val requestFile = tempFile.asRequestBody("image/*".toMediaType())
                                MultipartBody.Part.createFormData("images[]", tempFile.name, requestFile)
                            }

                            // ⬇️ ahora creamos parts de texto, uno por URL a borrar, con el nombre "deletedImageUrls[]"
                            val deletedParts: List<MultipartBody.Part> = deletedImageUrls.map { url ->
                                MultipartBody.Part.createFormData("deletedImageUrls[]", url)
                            }

                            val response = RetrofitClient.api(context).updateArticulo(
                                id = articleId,
                                method = methodPut,
                                name = namePart,
                                description = descriptionPart,
                                price = pricePart,
                                deletedImageUrls = deletedParts,
                                images = imageParts
                            )

                            withContext(Dispatchers.Main) {
                                isLoading = false
                                if (response.isSuccessful) {
                                    scaffoldState.snackbarHostState.showSnackbar("Artículo actualizado correctamente.")
                                    delay(500)
                                    navController.navigate("home") {
                                        popUpTo("edit/$articleId") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    scaffoldState.snackbarHostState.showSnackbar("Error al actualizar: ${response.code()}")
                                }
                            }

                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                scaffoldState.snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF388E3C))
            ) {
                Text(if (isLoading) "Actualizando..." else "Guardar cambios", color = Color.White)
            }
        }
    }
}

