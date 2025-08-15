package com.example.apiconnection.MODELS

data class Article(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val images: List<Image>,
    val user_id: Int? = null,      // 👈 para saber dueño
    val created: String? = null
)

data class Image(
    val id: Int,
    val url: String
)
