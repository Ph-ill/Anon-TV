package com.example.chan

import android.os.Bundle
import android.util.Log
import android.view.View
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
        title = getString(R.string.app_name)

        loadInitialThreads()
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Thread) {
                openMedia(item)
            }
        }
        setOnSearchClickedListener {
            loadInitialThreads()
        }
    }

    private fun loadInitialThreads() {
        allThreads.clear()
        loadedThreadCount = 0
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
        
        // Add scroll listener to detect when we need to load more
        setOnItemViewSelectedListener(object : OnItemViewSelectedListener {
            override fun onItemSelected(itemViewHolder: androidx.leanback.widget.Presenter.ViewHolder?, item: Any?, rowViewHolder: androidx.leanback.widget.RowPresenter.ViewHolder?, row: androidx.leanback.widget.Row?) {
                if (item is Thread) {
                    val currentIndex = allThreads.indexOf(item)
                    // Load more when user reaches the last few items
                    if (currentIndex >= allThreads.size - 3 && !isLoading) {
                        loadMoreThreads()
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