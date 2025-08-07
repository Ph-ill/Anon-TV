package com.example.chan

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Persists and restores the last-viewed media index per thread.
 * Data is stored in app-internal storage as JSON and survives app restarts.
 */
object ThreadPositionStore {

    private const val FILE_NAME = "thread_positions.json"

    // Configure Json similar to other usage
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    @Serializable
    private data class Positions(val map: Map<Long, Int>)

    private fun file(context: Context): File = File(context.filesDir, FILE_NAME)

    @Synchronized
    private fun readAll(context: Context): MutableMap<Long, Int> {
        return try {
            val f = file(context)
            if (!f.exists()) return mutableMapOf()
            val text = f.readText()
            if (text.isBlank()) return mutableMapOf()
            val positions = json.decodeFromString(Positions.serializer(), text)
            positions.map.toMutableMap()
        } catch (e: Exception) {
            Log.e("ThreadPositionStore", "Failed to read positions", e)
            mutableMapOf()
        }
    }

    @Synchronized
    private fun writeAll(context: Context, map: Map<Long, Int>) {
        try {
            val f = file(context)
            // Ensure directory exists
            f.parentFile?.mkdirs()
            val text = json.encodeToString(Positions(map))
            f.writeText(text)
        } catch (e: Exception) {
            Log.e("ThreadPositionStore", "Failed to write positions", e)
        }
    }

    /** Save/Update last viewed index for a thread */
    fun savePosition(context: Context, threadNo: Long, index: Int) {
        val clamped = if (index < 0) 0 else index
        val map = readAll(context)
        map[threadNo] = clamped
        writeAll(context, map)
        Log.d("ThreadPositionStore", "Saved thread $threadNo -> index $clamped")
    }

    /** Get saved index for a thread, or null if none saved */
    fun getPosition(context: Context, threadNo: Long): Int? {
        val map = readAll(context)
        return map[threadNo]
    }

    /** Clear position for a thread (optional utility) */
    fun clearPosition(context: Context, threadNo: Long) {
        val map = readAll(context)
        if (map.remove(threadNo) != null) writeAll(context, map)
    }
}
