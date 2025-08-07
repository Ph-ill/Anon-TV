package com.example.chan

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
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
    private var threadTitle: String = ""
    private var autoAdvanceJob: Job? = null
    private var threadNo: Long = -1L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set black background to prevent interface bleed-through
        view.setBackgroundColor(Color.BLACK)
        
        // Add thread title overlay
        addThreadTitleOverlay()
        
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
                        // Persist current position before leaving
                        savePositionSafely()
                        // Handle back button to return to thread list
                        parentFragmentManager.popBackStack()
                        return@setOnKeyListener true // Consume the event
                    }
                }
            }
            // Return true to consume all key events when MediaFragment is active
            true
        }

        // Play the initial media item
        if (mediaList.isNotEmpty()) {
            play(mediaList[currentMediaIndex])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaList = requireArguments().getParcelableArrayList(EXTRA_MEDIA_LIST)!!
        currentMediaIndex = requireArguments().getInt(EXTRA_CURRENT_MEDIA_INDEX)
        threadTitle = requireArguments().getString(EXTRA_THREAD_TITLE, "")
        threadNo = requireArguments().getLong(EXTRA_THREAD_NO, -1L)
        
        Log.d("MediaFragment", "Loaded ${mediaList.size} media items, starting at index $currentMediaIndex")
        Log.d("MediaFragment", "Thread title: $threadTitle")

        val glueHost = VideoSupportFragmentGlueHost(this)
        transportGlue = PlaybackTransportControlGlue(requireContext(), MediaPlayerAdapter(requireContext()))
        transportGlue.host = glueHost
        transportGlue.isSeekEnabled = true
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
        // Save position on pause to handle system or user navigation
        savePositionSafely()
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
    
    private fun addThreadTitleOverlay() {
        try {
            // Inflate the overlay layout
            val overlayView = android.view.LayoutInflater.from(requireContext())
                .inflate(R.layout.media_fragment_overlay, null)
            
            // Set the thread title text
            val titleTextView = overlayView.findViewById<TextView>(R.id.thread_title_text)
            titleTextView.text = threadTitle
            
            // Add the overlay to the view
            (view as? android.widget.FrameLayout)?.addView(overlayView, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            ))
        } catch (e: Exception) {
            Log.e("MediaFragment", "Could not add thread title overlay", e)
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
        
        // Start auto-advance monitoring
        startAutoAdvance()
    }
    
    private fun startAutoAdvance() {
        // Cancel any existing auto-advance job
        autoAdvanceJob?.cancel()
        
        autoAdvanceJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(1000) // Check every second
                
                try {
                    val currentPosition = transportGlue.playerAdapter.currentPosition
                    val duration = transportGlue.playerAdapter.duration
                    
                    // Only advance if video has a valid duration and position
                    if (duration > 0 && currentPosition > 0) {
                        val timeRemaining = duration - currentPosition
                        
                        // Check if video is within 1 second of ending
                        if (timeRemaining <= 1000) {
                            Log.d("MediaFragment", "Video ended, auto-advancing to next")
                            if (currentMediaIndex < mediaList.size - 1) {
                                next()
                            } else {
                                Log.d("MediaFragment", "Reached end of thread, stopping auto-advance")
                                break
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MediaFragment", "Error checking video progress", e)
                }
            }
        }
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
        autoAdvanceJob?.cancel()
        // Final attempt to save position
        savePositionSafely()
    }

    companion object {
        const val EXTRA_MEDIA_LIST = "extra_media_list"
        const val EXTRA_CURRENT_MEDIA_INDEX = "extra_current_media_index"
        const val EXTRA_THREAD_TITLE = "extra_thread_title"
        const val EXTRA_THREAD_NO = "extra_thread_no"
    }

    private fun savePositionSafely() {
        try {
            if (threadNo > 0 && mediaList.isNotEmpty()) {
                ThreadPositionStore.savePosition(requireContext(), threadNo, currentMediaIndex)
            }
        } catch (e: Exception) {
            Log.e("MediaFragment", "Failed to save thread position", e)
        }
    }
}