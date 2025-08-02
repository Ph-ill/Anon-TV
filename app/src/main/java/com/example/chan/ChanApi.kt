package com.example.chan

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ChanApi {

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getThreads(): List<Thread> {
        val response = client.get("https://a.4cdn.org/wsg/threads.json")
        val threads = response.body<List<Page>>()[0].threads
        return threads
    }

    suspend fun getMedia(threadNo: Long): List<Media> {
        val response = client.get("https://a.4cdn.org/wsg/thread/$threadNo.json")
        val posts = response.body<PostList>().posts
        return posts.mapNotNull { it.toMedia() }
    }

    @kotlinx.serialization.Serializable
    private data class Page(
        val page: Int,
        val threads: List<Thread>
    )

    @kotlinx.serialization.Serializable
    private data class PostList(
        val posts: List<Post>
    )

    @kotlinx.serialization.Serializable
    private data class Post(
        val tim: Long? = null,
        val filename: String? = null,
        val ext: String? = null
    ) {
        fun toMedia(): Media? {
            if (tim != null && filename != null && ext != null) {
                return Media(tim, filename, ext)
            }
            return null
        }
    }
}