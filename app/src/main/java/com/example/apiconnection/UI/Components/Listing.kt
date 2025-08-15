package com.example.apiconnection.UI.Components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.apiconnection.MODELS.Article
import com.example.apiconnection.Network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.accompanist.pager.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Listing(navController: NavController) {
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val api = remember { RetrofitClient.api(context) }

    var articulos by remember { mutableStateOf<List<Article>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var myId by remember { mutableStateOf<Int?>(null) }

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    // Obtener usuario actual y artículos
    LaunchedEffect(Unit) {
        try {
            myId = api.me().id
            val response = api.getArticulos()
            articulos = response.data
            isLoading = false
        } catch (e: Exception) {
            errorMessage = e.message
            isLoading = false
        }
    }

    fun onEliminarArticulo(id: Int) {
        eliminarArticulo(
            context = context,
            id = id,
            onSuccess = {
                articulos = articulos.filter { it.id != id }
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar("Artículo eliminado correctamente")
                }
            },
            onError = { msg ->
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(msg)
                }
            }
        )
    }

    Scaffold(scaffoldState = scaffoldState) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando artículos...", color = Color.Black)
                }

                errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = Color.Red)
                }

                articulos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron artículos", color = Color.Black)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(vertical = 4.dp),
                        state = listState
                    ) {
                        items(articulos, key = { it.id }) { articulo ->
                            val canEdit = (articulo.user_id != null && myId != null && articulo.user_id == myId)
                            ArticuloCard(
                                articulo = articulo,
                                navController = navController,
                                canEdit = canEdit
                            ) { articuloAEliminar ->
                                onEliminarArticulo(articuloAEliminar.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
@Composable
fun ArticuloCard(
    articulo: Article,
    navController: NavController,
    canEdit: Boolean,
    onDeleteConfirmed: (Article) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val dismissState = rememberDismissState(
        confirmStateChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> { // Intento borrar
                    if (canEdit) showDeleteDialog = true
                    false
                }
                DismissValue.DismissedToEnd -> { // Intento editar
                    if (canEdit) showEditDialog = true
                    false
                }
                else -> false
            }
        }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("¿Eliminar artículo?") },
            text = { Text("¿Estás seguro de que quieres eliminar \"${articulo.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteConfirmed(articulo)
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("No") }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("¿Editar artículo?") },
            text = { Text("¿Quieres modificar \"${articulo.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    navController.navigate("edit/${articulo.id}")
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("No") }
            }
        )
    }

    val pagerState = rememberPagerState()

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val color = when (dismissState.dismissDirection) {
                DismissDirection.EndToStart -> if (canEdit) Color.Red else Color.LightGray
                DismissDirection.StartToEnd -> if (canEdit) Color(0xFF1976D2) else Color.LightGray
                else -> Color.Transparent
            }

            val icon = when (dismissState.dismissDirection) {
                DismissDirection.EndToStart -> Icons.Default.Delete
                DismissDirection.StartToEnd -> Icons.Default.Edit
                else -> null
            }

            val alignment = when (dismissState.dismissDirection) {
                DismissDirection.EndToStart -> Alignment.CenterEnd
                DismissDirection.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        },
        dismissContent = {
            Card(
                backgroundColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .clickable { navController.navigate("show/${articulo.id}") }
                    .padding(vertical = 8.dp, horizontal = 12.dp)
                    .fillMaxWidth(),
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (articulo.images.isNotEmpty()) {
                        Column {
                            HorizontalPager(
                                count = articulo.images.size,
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) { page ->
                                val imageUrl = articulo.images[page].url
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Imagen del artículo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }

                            HorizontalPagerIndicator(
                                pagerState = pagerState,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 4.dp, bottom = 8.dp),
                                activeColor = Color.DarkGray,
                                inactiveColor = Color.LightGray
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(articulo.name, style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = articulo.description,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Precio: $${articulo.price}", color = Color(0xFF2E7D32))
                        if (!canEdit) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Solo el dueño puede editar o eliminar",
                                style = MaterialTheme.typography.caption,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    )
}

fun eliminarArticulo(
    context: Context,
    id: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.api(context).deleteArticulo(id)
            if (response.isSuccessful) {
                withContext(Dispatchers.Main) { onSuccess() }
            } else {
                withContext(Dispatchers.Main) {
                    onError("Error al eliminar artículo: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Excepción al eliminar artículo: ${e.message}")
            }
        }
    }
}
