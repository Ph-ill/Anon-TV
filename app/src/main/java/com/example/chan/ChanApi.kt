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
            val basicThreads = pages.flatMap { it.threads }
            Log.d("ChanApi", "Parsed ${basicThreads.size} basic threads.")
            
            // Fetch complete thread information for each thread
            val completeThreads = mutableListOf<Thread>()
            for (basicThread in basicThreads.take(10)) { // Limit to first 10 for performance
                try {
                    val threadDetail = getThreadDetail(basicThread.no)
                    if (threadDetail != null) {
                        completeThreads.add(threadDetail)
                    } else {
                        // Fallback to basic thread info
                        completeThreads.add(basicThread)
                    }
                } catch (e: Exception) {
                    Log.e("ChanApi", "Error fetching thread detail for ${basicThread.no}", e)
                    completeThreads.add(basicThread)
                }
            }
            
            completeThreads
        } catch (e: Exception) {
            Log.e("ChanApi", "Error fetching threads", e)
            emptyList()
        }
    }

    private suspend fun getThreadDetail(threadNo: Long): Thread? {
        return try {
            val response = client.get("https://a.4cdn.org/wsg/thread/$threadNo.json")
            val postList = response.body<PostList>()
            if (postList.posts.isNotEmpty()) {
                val firstPost = postList.posts.first()
                Thread(
                    no = threadNo,
                    sub = firstPost.sub,
                    com = firstPost.com,
                    semantic_url = firstPost.semantic_url,
                    replies = postList.posts.size - 1, // Subtract 1 for the OP
                    tim = firstPost.tim
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("ChanApi", "Error fetching thread detail for $threadNo", e)
            null
        }
    }

    suspend fun getMedia(threadNo: Long): List<Media> {
        return try {
            val response = client.get("https://a.4cdn.org/wsg/thread/$threadNo.json")
            val postList = response.body<PostList>()
            val media = postList.posts.mapNotNull { it.toMedia() }
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
        val semantic_url: String? = null,
        val sub: String? = null,
        val com: String? = null
    ) {
        fun toMedia(): Media? {
            if (tim != null && filename != null && ext != null) {
                return Media(tim, filename, ext)
            }
            return null
        }
    }
}