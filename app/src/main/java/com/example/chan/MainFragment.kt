package com.example.chan

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainFragment : BrowseSupportFragment() {

    companion object {
        // Cache thread data to survive activity recreation during theme changes
        private var cachedThreads = mutableListOf<Thread>()
        private var cachedLoadedCount = 0
        private var lastCacheTime = 0L
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes
        
        fun clearCache() {
            cachedThreads.clear()
            cachedLoadedCount = 0
            lastCacheTime = 0L
        }
    }

    private val api = ChanApi()
    private var allThreads = mutableListOf<Thread>()
    private var loadedThreadCount = 0
    private val pageSize = 30 // Testing 30 threads per load for better performance
    private var isLoading = false
    private var hasMoreThreads = true
    private var showingLoadingCard = false
    private var isOnLastCard = false
    private var lastCardSelectedTime = 0L
    private var currentSelectionPosition = 0
    private var autoLoadJob: kotlinx.coroutines.Job? = null
    
    // Keep references to the adapters so we can update them
    private var rowsAdapter: ArrayObjectAdapter? = null
    private var listRowAdapter: ArrayObjectAdapter? = null
    private var favouritesRowAdapter: ArrayObjectAdapter? = null
    private var settingsRowAdapter: ArrayObjectAdapter? = null
    


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = "Anon TV"
        // DO NOT disable headers/sidebar
        // setHeadersState(HEADERS_DISABLED)
        // setHeadersTransitionOnBackEnabled(true)
        Log.d("MainFragment", "onViewCreated: Sidebar should be visible, title set to Anon TV")
        

        
        // Ensure FavouritesManager is initialized first
        FavouritesManager.initialize(requireContext())
        
        // Ensure HiddenThreadsManager is initialized
        HiddenThreadsManager.initialize(requireContext())
        
        // Test basic file I/O
        val fileIOWorks = FavouritesManager.testFileIO()
        Log.d("MainFragment", "File I/O test result: $fileIOWorks")
        
        // Load initial content when the fragment is created
        loadInitialThreads()
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            Log.d("MainFragment", "*** CLICK LISTENER TRIGGERED *** Item: $item")
            when (item) {
                is Thread -> {
                    Log.d("MainFragment", "*** OPENING MEDIA FOR THREAD: ${item.no} ***")
                    openMedia(item)
                }
                is LoadingCard -> {
                    Log.d("MainFragment", "Loading card clicked, ignoring")
                }
                is MenuItem -> handleMenuItemClick(item)
            }
        }
        
        // For now, let's remove the complex long press detection and just get basic functionality working
        // We'll add a simple menu option instead of long press
    }

    private fun handleMenuItemClick(menuItem: MenuItem) {
        when (menuItem.id) {
            "theme_settings" -> openThemeSettings()
            "restore_hidden" -> restoreAllHiddenThreads()
            "close_app" -> closeApp()
        }
    }

    private fun openThemeSettings() {
        Log.d("MainFragment", "Opening theme settings")
        val themeSettingsFragment = ThemeSettingsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.main_frame, themeSettingsFragment)
            .addToBackStack("main")
            .commit()
    }

    private fun closeApp() {
        Log.d("MainFragment", "Closing app")
        activity?.finish()
    }
    
    private fun restoreAllHiddenThreads() {
        Log.d("MainFragment", "Restoring all hidden threads")
        val hiddenCount = HiddenThreadsManager.getHiddenThreadsCount()
        if (HiddenThreadsManager.clearAllHiddenThreads()) {
            // Refresh the thread list to show previously hidden threads
            refreshThreads()
            CustomToast.showSuccess(requireContext(), "Restored $hiddenCount hidden threads")
        } else {
            CustomToast.show(requireContext(), "No hidden threads to restore")
        }
    }
    
    private fun refreshThreads() {
        Log.d("MainFragment", "Refreshing threads to update visibility")
        // Force a refresh of the current threads display
        updateAdapter()
    }
    
    private fun loadFavourites() {
        Log.d("MainFragment", "loadFavourites called")
        Log.d("MainFragment", "favouritesRowAdapter is null: ${favouritesRowAdapter == null}")
        
        if (favouritesRowAdapter == null) {
            Log.e("MainFragment", "favouritesRowAdapter is null! Cannot load favourites")
            return
        }
        
        val favourites = FavouritesManager.getFavourites()
        Log.d("MainFragment", "FavouritesManager returned ${favourites.size} favourites")
        
        favouritesRowAdapter?.clear()
        favourites.forEach { thread ->
            Log.d("MainFragment", "Adding favourite thread: ${thread.no} - ${thread.sub}")
            favouritesRowAdapter?.add(thread)
        }
        
        // Force adapter to notify changes
        favouritesRowAdapter?.notifyArrayItemRangeChanged(0, favourites.size)
        
        Log.d("MainFragment", "Successfully loaded ${favourites.size} favourite threads into adapter")
        Log.d("MainFragment", "Adapter size after loading: ${favouritesRowAdapter?.size()}")
    }
    
    fun refreshFavourites() {
        loadFavourites()
    }
    
    fun refreshUI() {
        // Refresh both favourites and main thread list (to handle hidden threads)
        loadFavourites()
        
        // For main thread list, we need to recreate the adapter to filter hidden threads
        if (listRowAdapter != null) {
            Log.d("MainFragment", "refreshUI: Recreating thread adapter to apply hidden thread filters")
            listRowAdapter?.clear()
            val visibleThreads = allThreads.filter { !HiddenThreadsManager.isHidden(it) }
            for (thread in visibleThreads) {
                listRowAdapter?.add(thread)
            }
            listRowAdapter?.notifyArrayItemRangeChanged(0, visibleThreads.size)
        }
    }

    private fun showLoadingCard() {
        isLoading = true
        showingLoadingCard = true
        Log.d("MainFragment", "showLoadingCard: Adding loading card to adapter")
        addLoadingCardToAdapter()
    }

    private fun hideLoadingCard() {
        isLoading = false
        showingLoadingCard = false
        Log.d("MainFragment", "hideLoadingCard: Removing loading card from existing adapter")
        removeLoadingCardFromAdapter()
        
        // After removing loading card, we need to add the new threads that were loaded
        // The new threads are already in allThreads but not in the adapter
        val currentAdapterSize = listRowAdapter?.size() ?: 0
        val threadsInAdapter = currentAdapterSize
        val threadsInList = allThreads.size
        
        if (threadsInList > threadsInAdapter) {
            val newThreadsToAdd = allThreads.subList(threadsInAdapter, threadsInList)
            Log.d("MainFragment", "hideLoadingCard: Adding ${newThreadsToAdd.size} new threads to adapter")
            addNewThreadsToAdapter(newThreadsToAdd)
        }
    }

    private fun loadInitialThreads() {
        Log.d("MainFragment", "loadInitialThreads called")
        
        // Check if we have recent cached data (for theme changes)
        val currentTime = System.currentTimeMillis()
        val cacheAge = currentTime - lastCacheTime
        val hasFreshCache = cachedThreads.isNotEmpty() && cacheAge < CACHE_DURATION_MS
        
        if (hasFreshCache) {
            Log.d("MainFragment", "Using cached thread data (${cachedThreads.size} threads, cache age: ${cacheAge}ms)")
            
            // Restore from cache
            allThreads.clear()
            allThreads.addAll(cachedThreads)
            loadedThreadCount = cachedLoadedCount
            
            // Set up initial state without loading
            isLoading = false
            hasMoreThreads = true
            showingLoadingCard = false
            isOnLastCard = false
            lastCardSelectedTime = 0L
            currentSelectionPosition = 0
            autoLoadJob?.cancel()
            
            // Hide loading spinner and update adapter immediately
            hideLoadingSpinner()
            updateAdapter()
        } else {
            Log.d("MainFragment", "No fresh cache available, loading from API")
            
            // Clear cache and load fresh data
            clearCache()
            allThreads.clear()
            loadedThreadCount = 0
            isLoading = true
            hasMoreThreads = true
            showingLoadingCard = false
            isOnLastCard = false
            lastCardSelectedTime = 0L
            currentSelectionPosition = 0
            autoLoadJob?.cancel()
            showLoadingSpinner()
            loadMoreThreads()
        }
    }

    private fun loadMoreThreads() {
        Log.d("MainFragment", "loadMoreThreads called")
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("MainFragment", "Starting API call...")
                val newThreads = api.getThreads(loadedThreadCount, pageSize)
                Log.d("MainFragment", "API call completed, got ${newThreads.size} threads")
                if (newThreads.isNotEmpty()) {
                    allThreads.addAll(newThreads)
                    loadedThreadCount += newThreads.size
                    
                    // Update cache with current thread data
                    cachedThreads.clear()
                    cachedThreads.addAll(allThreads)
                    cachedLoadedCount = loadedThreadCount
                    lastCacheTime = System.currentTimeMillis()
                    Log.d("MainFragment", "Updated cache with ${cachedThreads.size} threads")
                    
                    Log.d("MainFragment", "Updating adapter with ${allThreads.size} total threads")
                    Log.d("MainFragment", "allThreads size: ${allThreads.size}, loadedThreadCount: $loadedThreadCount")
                    
                    if (rowsAdapter == null) {
                        // First load - create the adapter
                        Log.d("MainFragment", "First load - creating adapter")
                        updateAdapter()
                    } else {
                        // Subsequent loads - add to existing adapter
                        if (showingLoadingCard) {
                            Log.d("MainFragment", "Removing loading card and adding new threads")
                            hideLoadingCard()
                        } else {
                            Log.d("MainFragment", "Adding new threads to existing adapter")
                            addNewThreadsToAdapter(newThreads)
                        }
                    }
                } else {
                    Log.d("MainFragment", "No threads returned from API")
                    hasMoreThreads = false
                    if (showingLoadingCard) {
                        hideLoadingCard()
                    }
                    Toast.makeText(context, "No more threads available", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("MainFragment", "Error loading threads", e)
                hasMoreThreads = false
                if (showingLoadingCard) {
                    hideLoadingCard()
                }
                Toast.makeText(context, "Error loading threads", Toast.LENGTH_SHORT).show()
            } finally {
                if (!showingLoadingCard) {
                    isLoading = false
                    hideLoadingSpinner()
                }
                Log.d("MainFragment", "Loading completed")
            }
        }
    }

    private fun createInitialAdapter() {
        Log.d("MainFragment", "createInitialAdapter: Creating new adapter")
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter { refreshUI() }
        listRowAdapter = ArrayObjectAdapter(cardPresenter)
        
        // Create favourites adapter using the same CardPresenter
        favouritesRowAdapter = ArrayObjectAdapter(cardPresenter)
        
        // Create settings adapter using the same CardPresenter
        settingsRowAdapter = ArrayObjectAdapter(cardPresenter)
        
        // Add settings menu items
        val themeSettingsItem = MenuItem(
            id = "theme_settings",
            title = "Theme Settings",
            description = "Choose your preferred theme and appearance",
            icon = R.drawable.ic_theme_settings,
            action = { openThemeSettings() }
        )
        
        val restoreHiddenItem = MenuItem(
            id = "restore_hidden",
            title = "Restore Hidden Threads",
            description = "Unhide all previously hidden threads",
            icon = R.drawable.ic_restore_hidden,
            action = { restoreAllHiddenThreads() }
        )
        
        val closeAppItem = MenuItem(
            id = "close_app",
            title = "Close App",
            description = "Exit the application",
            icon = R.drawable.ic_close_app,
            action = { closeApp() }
        )
        
        settingsRowAdapter?.add(themeSettingsItem)
        settingsRowAdapter?.add(restoreHiddenItem)
        settingsRowAdapter?.add(closeAppItem)
        
        val header = HeaderItem(0, "/wsg/")
        val favouritesHeader = HeaderItem(1, "Favourites")
        val settingsHeader = HeaderItem(2, "App Settings")
        
        rowsAdapter?.add(ListRow(header, listRowAdapter!!))
        rowsAdapter?.add(ListRow(favouritesHeader, favouritesRowAdapter!!))
        rowsAdapter?.add(ListRow(settingsHeader, settingsRowAdapter!!))
        adapter = rowsAdapter
        
        // Now that adapters are created, load favourites
        Log.d("MainFragment", "Adapters created, now loading favourites...")
        loadFavourites()
        
        setOnItemViewSelectedListener(object : OnItemViewSelectedListener {
            override fun onItemSelected(itemViewHolder: androidx.leanback.widget.Presenter.ViewHolder?, item: Any?, rowViewHolder: androidx.leanback.widget.RowPresenter.ViewHolder?, row: androidx.leanback.widget.Row?) {
                when (item) {
                    is Thread -> {
                        val currentIndex = allThreads.indexOf(item)
                        currentSelectionPosition = currentIndex
                        val wasOnLastCard = isOnLastCard
                        isOnLastCard = (currentIndex == allThreads.size - 1)
                        Log.d("MainFragment", "Selected thread at index $currentIndex, isOnLastCard: $isOnLastCard, currentSelectionPosition: $currentSelectionPosition")
                        
                        // Cancel any existing auto-load job
                        autoLoadJob?.cancel()
                        
                        if (isOnLastCard && !wasOnLastCard) {
                            lastCardSelectedTime = System.currentTimeMillis()
                            Log.d("MainFragment", "User reached last item, starting auto-load timer")
                            
                            autoLoadJob = CoroutineScope(Dispatchers.Main).launch {
                                delay(1) // Changed from 1000ms to 1ms for immediate loading
                                if (isOnLastCard && !isLoading && hasMoreThreads && System.currentTimeMillis() - lastCardSelectedTime >= 1) {
                                    Log.d("MainFragment", "Auto-loading more threads after delay")
                                    showLoadingCard()
                                    loadMoreThreads()
                                }
                            }
                        }
                    }
                    is LoadingCard -> {
                        Log.d("MainFragment", "User selected loading card")
                        currentSelectionPosition = allThreads.size // Position of loading card
                        isOnLastCard = false
                        autoLoadJob?.cancel()
                    }
                    else -> {
                        isOnLastCard = false
                        autoLoadJob?.cancel()
                    }
                }
            }
        })
    }

    private fun addNewThreadsToAdapter(newThreads: List<Thread>) {
        Log.d("MainFragment", "addNewThreadsToAdapter: Adding ${newThreads.size} new threads to existing adapter")
        listRowAdapter?.let { adapter ->
            val startSize = adapter.size()
            // Filter out hidden threads
            val visibleNewThreads = newThreads.filter { !HiddenThreadsManager.isHidden(it) }
            Log.d("MainFragment", "addNewThreadsToAdapter: Adding ${visibleNewThreads.size} visible threads (${newThreads.size - visibleNewThreads.size} hidden)")
            for (thread in visibleNewThreads) {
                adapter.add(thread)
                Log.d("MainFragment", "Added thread ${thread.no} to adapter")
            }
            val endSize = adapter.size()
            Log.d("MainFragment", "Adapter size changed from $startSize to $endSize")
            
            // Force the adapter to notify of changes
            adapter.notifyArrayItemRangeChanged(startSize, visibleNewThreads.size)
        }
    }

    private fun addLoadingCardToAdapter() {
        Log.d("MainFragment", "addLoadingCardToAdapter: Adding loading card to existing adapter")
        listRowAdapter?.add(LoadingCard())
    }

    private fun removeLoadingCardFromAdapter() {
        Log.d("MainFragment", "removeLoadingCardFromAdapter: Removing loading card from existing adapter")
        listRowAdapter?.let { adapter ->
            // Find and remove the loading card
            for (i in adapter.size() - 1 downTo 0) {
                if (adapter.get(i) is LoadingCard) {
                    adapter.remove(adapter.get(i))
                    break
                }
            }
        }
    }

    private fun updateAdapter() {
        Log.d("MainFragment", "updateAdapter: Setting up row with ${allThreads.size} threads")
        createInitialAdapter()
        
        // Add all threads to the adapter, filtering out hidden ones
        val visibleThreads = allThreads.filter { !HiddenThreadsManager.isHidden(it) }
        Log.d("MainFragment", "updateAdapter: Adding ${visibleThreads.size} visible threads (${allThreads.size - visibleThreads.size} hidden)")
        for (thread in visibleThreads) {
            listRowAdapter?.add(thread)
        }
    }

    private fun showLoadingSpinner() {
        Log.d("MainFragment", "showLoadingSpinner: Showing spinner and disabling input")
        view?.isEnabled = false
        (activity as? MainActivity)?.loadingSpinner?.visibility = android.view.View.VISIBLE
        adapter = null
    }

    private fun hideLoadingSpinner() {
        Log.d("MainFragment", "hideLoadingSpinner: Hiding spinner and enabling input")
        view?.isEnabled = true
        (activity as? MainActivity)?.loadingSpinner?.visibility = android.view.View.GONE
    }

    private fun openMedia(thread: Thread) {
        Log.d("MainFragment", "openMedia: Opening media for thread ${thread.no}")
        CoroutineScope(Dispatchers.Main).launch {
            val media = api.getMedia(thread.no)
            if (media.isNotEmpty()) {
                val fragment = MediaFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList(MediaFragment.EXTRA_MEDIA_LIST, ArrayList(media))

                // Restore previously saved position if available
                val savedIndex = ThreadPositionStore.getPosition(requireContext(), thread.no) ?: 0
                val startIndex = savedIndex.coerceIn(0, media.size - 1)
                bundle.putInt(MediaFragment.EXTRA_CURRENT_MEDIA_INDEX, startIndex)

                // Pass thread id for saving on exit
                bundle.putLong(MediaFragment.EXTRA_THREAD_NO, thread.no)
                
                // Get thread title from subject or comment
                val threadTitle = thread.sub ?: thread.com ?: "Thread ${thread.no}"
                bundle.putString(MediaFragment.EXTRA_THREAD_TITLE, threadTitle)
                
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}