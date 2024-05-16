package com.example.mathis

import kotlinx.serialization.Serializable


@Serializable
data class MovieResponse(
    val page: Int,
    val results: List<Movie>
)

@Serializable
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val original_title: String,
    val vote_count: Int,
)

@Serializable
data class MovieDetail(
    val id: Int,
    val original_title: String,
    val overview: String,
    val poster_path: String?,
    val release_date: String,
    val title: String,
    val vote_average: Double,
    val vote_count: Int
)

@Serializable
data class FavoriteBody(
    val media_type: String,
    val media_id: Int,
    val favorite: Boolean
)