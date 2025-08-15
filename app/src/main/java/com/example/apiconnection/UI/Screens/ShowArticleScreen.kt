package com.example.apiconnection.UI.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.apiconnection.MODELS.Article
import com.example.apiconnection.Network.RetrofitClient
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ShowArticleScreen(navController: NavController, articleId: Int) {
    var article by remember { mutableStateOf<Article?>(null) }
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.api(context).getArticulos()
            article = response.data.find { it.id == articleId }
        } catch (e: Exception) {
            scaffoldState.snackbarHostState.showSnackbar("Error al cargar artículo.")
            navController.popBackStack()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Artículo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        article?.let {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                if (it.images.isNotEmpty()) {
                    HorizontalPager(
                        count = it.images.size,
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) { page ->
                        AsyncImage(
                            model = it.images[page].url,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp),
                        activeColor = Color.DarkGray,
                        inactiveColor = Color.LightGray
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = it.name,
                        style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Precio: $${it.price}",
                        style = MaterialTheme.typography.subtitle1,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = it.description,
                        style = MaterialTheme.typography.body1,
                        lineHeight = 22.sp
                    )
                }
            }
        } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
