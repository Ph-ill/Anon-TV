package com.example.chan

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MediaFragment : VideoSupportFragment() {

    private lateinit var transportGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private lateinit var mediaList: List<Media>
    private var currentMediaIndex = 0
    private var hideControlsJob: Job? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Ensure this fragment has focus
        view.requestFocus()
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        
        view.setOnKeyListener { _, keyCode, event ->
            Log.d("MediaFragment", "Key event received: $keyCode")
            if (event.action == KeyEvent.ACTION_DOWN) {
                // Show controls when user interacts
                showControls()
                
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_RIGHT -> {
                        Log.d("MediaFragment", "Right button pressed - navigating to next video")
                        next()
                        return@setOnKeyListener true // Consume the event
                    }
                    KeyEvent.KEYCODE_DPAD_LEFT -> {
                        Log.d("MediaFragment", "Left button pressed - navigating to previous video")
                        previous()
                        return@setOnKeyListener true // Consume the event
                    }
                    KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                        Log.d("MediaFragment", "Enter button pressed - toggling play/pause")
                        // Toggle play/pause and show controls
                        if (transportGlue.isPlaying) {
                            transportGlue.pause()
                        } else {
                            transportGlue.play()
                        }
                        showControls()
                        return@setOnKeyListener true // Consume the event
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        Log.d("MediaFragment", "Back button pressed - returning to thread list")
                        // Handle back button to return to thread list
                        parentFragmentManager.popBackStack()
                        return@setOnKeyListener true // Consume the event
                    }
                }
            }
            // Return true to consume all key events when MediaFragment is active
            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaList = requireArguments().getParcelableArrayList(EXTRA_MEDIA_LIST)!!
        currentMediaIndex = requireArguments().getInt(EXTRA_CURRENT_MEDIA_INDEX)
        
        Log.d("MediaFragment", "Loaded ${mediaList.size} media items, starting at index $currentMediaIndex")

        val glueHost = VideoSupportFragmentGlueHost(this)
        transportGlue = PlaybackTransportControlGlue(requireContext(), MediaPlayerAdapter(requireContext()))
        transportGlue.host = glueHost
        transportGlue.isSeekEnabled = true
        
        if (mediaList.isNotEmpty()) {
            play(mediaList[currentMediaIndex])
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure focus when fragment becomes active
        view?.requestFocus()
        Log.d("MediaFragment", "Fragment resumed - requesting focus")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MediaFragment", "Fragment paused")
    }

    private fun showControls() {
        Log.d("MediaFragment", "Showing video controls")
        transportGlue.host.showControlsOverlay(true)
        
        // Cancel any existing hide job
        hideControlsJob?.cancel()
        
        // Start new hide job
        hideControlsJob = CoroutineScope(Dispatchers.Main).launch {
            delay(1000) // Reduced from 3000ms to 1000ms
            Log.d("MediaFragment", "Auto-hiding video controls")
            transportGlue.host.hideControlsOverlay(true)
        }
    }

    private fun play(media: Media) {
        Log.d("MediaFragment", "Playing media: ${media.filename} (${currentMediaIndex + 1}/${mediaList.size})")
        transportGlue.title = "${media.filename} (${currentMediaIndex + 1}/${mediaList.size})"
        
        val mediaUrl = "https://i.4cdn.org/wsg/${media.tim}${media.ext}"
        Log.d("MediaFragment", "Loading media URL: $mediaUrl")
        
        transportGlue.playerAdapter.setDataSource(Uri.parse(mediaUrl))
        transportGlue.playWhenPrepared()
        
        // Show controls initially when video starts
        showControls()
    }

    private fun next() {
        if (currentMediaIndex < mediaList.size - 1) {
            currentMediaIndex++
            Log.d("MediaFragment", "Next: moving to index $currentMediaIndex")
            play(mediaList[currentMediaIndex])
        } else {
            Log.d("MediaFragment", "Next: already at last item")
        }
    }

    private fun previous() {
        if (currentMediaIndex > 0) {
            currentMediaIndex--
            Log.d("MediaFragment", "Previous: moving to index $currentMediaIndex")
            play(mediaList[currentMediaIndex])
        } else {
            Log.d("MediaFragment", "Previous: already at first item")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideControlsJob?.cancel()
    }

    companion object {
        const val EXTRA_MEDIA_LIST = "extra_media_list"
        const val EXTRA_CURRENT_MEDIA_INDEX = "extra_current_media_index"
    }
}