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

    private val api = ChanApi()
    private var allThreads = mutableListOf<Thread>()
    private var loadedThreadCount = 0
    private val pageSize = 10 // Changed back from 5 to 10 for more content per load
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = "Anon TV"
        // DO NOT disable headers/sidebar
        // setHeadersState(HEADERS_DISABLED)
        // setHeadersTransitionOnBackEnabled(true)
        Log.d("MainFragment", "onViewCreated: Sidebar should be visible, title set to Anon TV")
        // Load initial content when the fragment is created
        loadInitialThreads()
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Thread -> openMedia(item)
                is LoadingCard -> {
                    Log.d("MainFragment", "Loading card clicked, ignoring")
                }
            }
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
        Log.d("MainFragment", "hideLoadingCard: Removing loading card from adapter")
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
        val cardPresenter = CardPresenter()
        listRowAdapter = ArrayObjectAdapter(cardPresenter)
        
        val header = HeaderItem(0, "/wsg/")
        rowsAdapter?.add(ListRow(header, listRowAdapter!!))
        adapter = rowsAdapter
        
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
                                delay(1000) // Reduced from 2000ms to 1000ms
                                if (isOnLastCard && !isLoading && hasMoreThreads && System.currentTimeMillis() - lastCardSelectedTime >= 1000) {
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
            for (thread in newThreads) {
                adapter.add(thread)
                Log.d("MainFragment", "Added thread ${thread.no} to adapter")
            }
            val endSize = adapter.size()
            Log.d("MainFragment", "Adapter size changed from $startSize to $endSize")
            
            // Force the adapter to notify of changes
            adapter.notifyArrayItemRangeChanged(startSize, newThreads.size)
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
        
        // Add all threads to the adapter
        for (thread in allThreads) {
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
                bundle.putInt(MediaFragment.EXTRA_CURRENT_MEDIA_INDEX, 0)
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}