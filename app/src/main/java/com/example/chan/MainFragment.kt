package com.example.chan

import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainFragment : BrowseSupportFragment() {

    private val api = ChanApi()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = getString(R.string.app_name)

        loadThreads()
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Thread) {
                openMedia(item)
            }
        }
        setOnSearchClickedListener {
            loadThreads()
        }
    }

    private fun loadThreads() {
        CoroutineScope(Dispatchers.Main).launch {
            val threads = api.getThreads()
            val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
            val cardPresenter = CardPresenter()
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            for (thread in threads) {
                listRowAdapter.add(thread)
            }
            val header = HeaderItem(0, "Threads")
            rowsAdapter.add(ListRow(header, listRowAdapter))
            adapter = rowsAdapter
        }
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