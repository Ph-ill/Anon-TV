package com.example.chan

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class HiddenThread(
    val thread: Thread,
    val hiddenTimestamp: Long = System.currentTimeMillis()
)

object HiddenThreadsManager {
    private const val PREFS_NAME = "hidden_threads"
    private const val KEY_HIDDEN_DATA = "hidden_data"
    private var hiddenThreads = mutableListOf<HiddenThread>()
    private var context: Context? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        Log.d("HiddenThreadsManager", "initialize() called with context: ${context.javaClass.simpleName}")
        Log.d("HiddenThreadsManager", "Previously initialized: $isInitialized")

        this.context = context

        if (!isInitialized) {
            loadHiddenThreads()
            isInitialized = true
            Log.d("HiddenThreadsManager", "First time initialization complete")
        } else {
            Log.d("HiddenThreadsManager", "Already initialized, current hidden threads count: ${hiddenThreads.size}")
        }
    }

    fun hideThread(thread: Thread): Boolean {
        if (isHidden(thread)) {
            Log.d("HiddenThreadsManager", "Thread ${thread.no} is already hidden")
            return false
        }
        hiddenThreads.add(HiddenThread(thread))
        saveHiddenThreads()
        Log.d("HiddenThreadsManager", "Hidden thread: ${thread.no}. Total hidden: ${hiddenThreads.size}")
        return true
    }

    fun unhideThread(thread: Thread): Boolean {
        val removed = hiddenThreads.removeAll { it.thread.no == thread.no }
        if (removed) {
            saveHiddenThreads()
            Log.d("HiddenThreadsManager", "Unhidden thread: ${thread.no}. Total hidden: ${hiddenThreads.size}")
        } else {
            Log.d("HiddenThreadsManager", "Thread ${thread.no} not found in hidden threads to remove")
        }
        return removed
    }

    fun isHidden(thread: Thread): Boolean {
        return hiddenThreads.any { it.thread.no == thread.no }
    }

    fun getHiddenThreads(): List<Thread> {
        Log.d("HiddenThreadsManager", "getHiddenThreads() called, returning ${hiddenThreads.size} threads")
        return hiddenThreads.sortedByDescending { it.hiddenTimestamp }.map { it.thread }
    }

    fun clearAllHiddenThreads(): Boolean {
        val hadHiddenThreads = hiddenThreads.isNotEmpty()
        hiddenThreads.clear()
        saveHiddenThreads()
        Log.d("HiddenThreadsManager", "Cleared all hidden threads")
        return hadHiddenThreads
    }

    fun getHiddenThreadsCount(): Int {
        return hiddenThreads.size
    }

    private fun saveHiddenThreads() {
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val json = Json.encodeToString(hiddenThreads)
                Log.d("HiddenThreadsManager", "Attempting to save hidden threads to SharedPreferences")
                Log.d("HiddenThreadsManager", "JSON content: $json")

                prefs.edit()
                    .putString(KEY_HIDDEN_DATA, json)
                    .apply()

                Log.d("HiddenThreadsManager", "Successfully saved ${hiddenThreads.size} hidden threads to SharedPreferences")
            } catch (e: Exception) {
                Log.e("HiddenThreadsManager", "Error saving hidden threads to SharedPreferences", e)
            }
        } ?: run {
            Log.e("HiddenThreadsManager", "Context is null when trying to save hidden threads")
        }
    }

    private fun loadHiddenThreads() {
        context?.let { ctx ->
            try {
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val json = prefs.getString(KEY_HIDDEN_DATA, null)

                Log.d("HiddenThreadsManager", "Attempting to load hidden threads from SharedPreferences")
                Log.d("HiddenThreadsManager", "JSON from prefs: $json")

                if (json != null && json.isNotEmpty()) {
                    hiddenThreads = Json.decodeFromString<MutableList<HiddenThread>>(json)
                    Log.d("HiddenThreadsManager", "Successfully loaded ${hiddenThreads.size} hidden threads from SharedPreferences")
                } else {
                    Log.d("HiddenThreadsManager", "No hidden threads data found in SharedPreferences, starting with empty list")
                    hiddenThreads = mutableListOf()
                }
            } catch (e: Exception) {
                Log.e("HiddenThreadsManager", "Error loading hidden threads from SharedPreferences", e)
                hiddenThreads = mutableListOf()
            }
        } ?: run {
            Log.e("HiddenThreadsManager", "Context is null when trying to load hidden threads")
        }
    }
}
