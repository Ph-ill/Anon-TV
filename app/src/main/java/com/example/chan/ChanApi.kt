package com.example.chan

import android.util.Log
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
                encodeDefaults = true
            })
        }
    }

    suspend fun getThreads(): List<Thread> {
        return try {
            val response = client.get("https://a.4cdn.org/wsg/threads.json")
            val pages = response.body<List<Page>>()
            val threads = pages.flatMap { it.threads }
            Log.d("ChanApi", "Parsed ${threads.size} threads.")
            threads
        } catch (e: Exception) {
            Log.e("ChanApi", "Error fetching threads", e)
            emptyList()
        }
    }

    suspend fun getMedia(threadNo: Long): List<Media> {
        return try {
            val response = client.get("https://a.4cdn.org/wsg/thread/$threadNo.json")
            val postList = response.body<PostList>()
            val media = postList.posts.mapNotNull { it.toMedia(threadNo) }
            Log.d("ChanApi", "Parsed ${media.size} media items for thread $threadNo.")
            media
        } catch (e: Exception) {
            Log.e("ChanApi", "Error fetching media for thread $threadNo", e)
            emptyList()
        }
    }

    @kotlinx.serialization.Serializable
    internal data class Page(
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
        val ext: String? = null,
        val semantic_url: String? = null
    ) {
        fun toMedia(threadNo: Long): Media? {
            if (tim != null && filename != null && ext != null) {
                return Media(tim, filename, ext)
            }
            return null
        }
    }
}