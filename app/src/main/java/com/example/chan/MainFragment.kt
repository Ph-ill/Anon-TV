package com.example.chan

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.leanback.widget.OnItemViewSelectedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragment : BrowseSupportFragment() {

    private val api = ChanApi()
    private var allThreads = mutableListOf<Thread>()
    private var loadedThreadCount = 0
    private val pageSize = 10
    private var isLoading = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = "Anon TV"
        
        // Show loading indicator initially
        showLoadingIndicator()
        loadInitialThreads()
        
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Thread) {
                openMedia(item)
            }
        }
        setOnSearchClickedListener {
            loadInitialThreads()
        }
        
        // Handle key events for loading more content
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT && !isLoading) {
                val selectedPosition = getSelectedPosition()
                if (selectedPosition >= 0 && selectedPosition == allThreads.size - 1) {
                    // User pressed right on the last item, load more
                    Log.d("MainFragment", "Right pressed on last item, loading more threads")
                    showLoadingIndicator()
                    loadMoreThreads()
                    return@setOnKeyListener true
                }
            }
            false
        }
    }

    private fun showLoadingIndicator() {
        // Disable user input during loading
        view?.isEnabled = false
        
        // Show a simple loading message in the adapter
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        
        val header = HeaderItem(0, "/wsg/")
        rowsAdapter.add(ListRow(header, listRowAdapter))
        adapter = rowsAdapter
    }

    private fun loadInitialThreads() {
        allThreads.clear()
        loadedThreadCount = 0
        isLoading = true
        loadMoreThreads()
    }

    private fun loadMoreThreads() {
        if (isLoading) return
        
        CoroutineScope(Dispatchers.Main).launch {
            isLoading = true
            val newThreads = api.getThreads(loadedThreadCount, pageSize)
            Log.d("MainFragment", "Fetched ${newThreads.size} new threads.")
            
            if (newThreads.isNotEmpty()) {
                allThreads.addAll(newThreads)
                loadedThreadCount += newThreads.size
                updateAdapter()
            }
            isLoading = false
            // Re-enable user input after loading
            view?.isEnabled = true
        }
    }

    private fun updateAdapter() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)
        
        for (thread in allThreads) {
            listRowAdapter.add(thread)
        }
        
        val header = HeaderItem(0, "/wsg/")
        rowsAdapter.add(ListRow(header, listRowAdapter))
        adapter = rowsAdapter
        
        // Add scroll listener to detect when user reaches the last item
        setOnItemViewSelectedListener(object : OnItemViewSelectedListener {
            override fun onItemSelected(itemViewHolder: androidx.leanback.widget.Presenter.ViewHolder?, item: Any?, rowViewHolder: androidx.leanback.widget.RowPresenter.ViewHolder?, row: androidx.leanback.widget.Row?) {
                if (item is Thread) {
                    val currentIndex = allThreads.indexOf(item)
                    // Check if this is the last item
                    if (currentIndex == allThreads.size - 1) {
                        // User is on the last item, enable loading on right press
                        Log.d("MainFragment", "User reached last item, ready to load more on right press")
                    }
                }
            }
        })
    }

    private fun openMedia(thread: Thread) {
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