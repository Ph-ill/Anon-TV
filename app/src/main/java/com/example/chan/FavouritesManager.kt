package com.example.chan

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

@Serializable
data class FavouriteThread(
    val thread: Thread,
    val addedTimestamp: Long = System.currentTimeMillis()
)

object FavouritesManager {
    private const val FAVOURITES_FILE = "favourites.json"
    private var favouriteThreads = mutableListOf<FavouriteThread>()
    private var context: Context? = null
    private var isInitialized = false
    
    fun initialize(context: Context) {
        Log.d("FavouritesManager", "initialize() called with context: ${context.javaClass.simpleName}")
        Log.d("FavouritesManager", "Previously initialized: $isInitialized")
        
        this.context = context
        Log.d("FavouritesManager", "Files directory: ${context.filesDir}")
        
        if (!isInitialized) {
            loadFavourites()
            isInitialized = true
            Log.d("FavouritesManager", "First time initialization complete")
        } else {
            Log.d("FavouritesManager", "Already initialized, current favourites count: ${favouriteThreads.size}")
        }
    }
    
    fun addFavourite(thread: Thread): Boolean {
        // Check if thread is already in favourites
        if (isFavourite(thread)) {
            Log.d("FavouritesManager", "Thread ${thread.no} is already in favourites")
            return false
        }
        
        val favouriteThread = FavouriteThread(thread = thread)
        favouriteThreads.add(favouriteThread)
        saveFavourites()
        Log.d("FavouritesManager", "Added thread ${thread.no} to favourites")
        return true
    }
    
    fun removeFavourite(thread: Thread): Boolean {
        val removed = favouriteThreads.removeAll { it.thread.no == thread.no }
        if (removed) {
            saveFavourites()
            Log.d("FavouritesManager", "Removed thread ${thread.no} from favourites")
        }
        return removed
    }
    
    fun isFavourite(thread: Thread): Boolean {
        return favouriteThreads.any { it.thread.no == thread.no }
    }
    
    fun getFavourites(): List<Thread> {
        Log.d("FavouritesManager", "getFavourites() called, returning ${favouriteThreads.size} threads")
        return favouriteThreads.sortedByDescending { it.addedTimestamp }.map { it.thread }
    }
    
    // Debug method to test file I/O
    fun testFileIO(): Boolean {
        context?.let { ctx ->
            try {
                val testFile = File(ctx.filesDir, "test.txt")
                testFile.writeText("test123")
                val readBack = testFile.readText()
                Log.d("FavouritesManager", "File I/O test: wrote 'test123', read back '$readBack'")
                return readBack == "test123"
            } catch (e: Exception) {
                Log.e("FavouritesManager", "File I/O test failed", e)
                return false
            }
        }
        return false
    }
    
    fun getFavouritesCount(): Int {
        return favouriteThreads.size
    }
    
    private fun saveFavourites() {
        context?.let { ctx ->
            try {
                // Use SharedPreferences instead of file storage for better reliability
                val prefs = ctx.getSharedPreferences("favourites", Context.MODE_PRIVATE)
                val json = Json.encodeToString(favouriteThreads)
                Log.d("FavouritesManager", "Attempting to save favourites to SharedPreferences")
                Log.d("FavouritesManager", "JSON content: $json")
                
                prefs.edit()
                    .putString("favourites_data", json)
                    .apply()
                    
                Log.d("FavouritesManager", "Successfully saved ${favouriteThreads.size} favourites to SharedPreferences")
            } catch (e: Exception) {
                Log.e("FavouritesManager", "Error saving favourites to SharedPreferences", e)
            }
        } ?: run {
            Log.e("FavouritesManager", "Context is null when trying to save favourites")
        }
    }
    
    private fun loadFavourites() {
        context?.let { ctx ->
            try {
                // Use SharedPreferences instead of file storage for better reliability
                val prefs = ctx.getSharedPreferences("favourites", Context.MODE_PRIVATE)
                val json = prefs.getString("favourites_data", null)
                
                Log.d("FavouritesManager", "Attempting to load favourites from SharedPreferences")
                Log.d("FavouritesManager", "JSON from prefs: $json")
                
                if (json != null && json.isNotEmpty()) {
                    favouriteThreads = Json.decodeFromString<MutableList<FavouriteThread>>(json)
                    Log.d("FavouritesManager", "Successfully loaded ${favouriteThreads.size} favourites from SharedPreferences")
                } else {
                    Log.d("FavouritesManager", "No favourites data found in SharedPreferences, starting with empty list")
                    favouriteThreads = mutableListOf()
                }
            } catch (e: Exception) {
                Log.e("FavouritesManager", "Error loading favourites from SharedPreferences", e)
                favouriteThreads = mutableListOf()
            }
        } ?: run {
            Log.e("FavouritesManager", "Context is null when trying to load favourites")
        }
    }
    
    fun clearAllFavourites() {
        favouriteThreads.clear()
        saveFavourites()
        Log.d("FavouritesManager", "Cleared all favourites")
    }
}
