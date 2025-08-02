package com.example.chan

import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.leanback.app.VideoSupportFragment
import androidx.leanback.app.VideoSupportFragmentGlueHost
import androidx.leanback.media.MediaPlayerAdapter
import androidx.leanback.media.PlaybackTransportControlGlue

class MediaFragment : VideoSupportFragment() {

    private lateinit var transportGlue: PlaybackTransportControlGlue<MediaPlayerAdapter>
    private lateinit var mediaList: List<Media>
    private var currentMediaIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    next()
                    return@setOnKeyListener true
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    previous()
                    return@setOnKeyListener true
                }
            }
            false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaList = requireArguments().getParcelableArrayList(EXTRA_MEDIA_LIST)!!
        currentMediaIndex = requireArguments().getInt(EXTRA_CURRENT_MEDIA_INDEX)

        val glueHost = VideoSupportFragmentGlueHost(this)
        transportGlue = PlaybackTransportControlGlue(requireContext(), MediaPlayerAdapter(requireContext()))
        transportGlue.host = glueHost
        transportGlue.isSeekEnabled = true
        play(mediaList[currentMediaIndex])
    }

    private fun play(media: Media) {
        transportGlue.title = media.filename
        transportGlue.playerAdapter.setDataSource(Uri.parse("https://i.4cdn.org/wsg/${media.tim}${media.ext}"))
        transportGlue.playWhenPrepared()
    }

    private fun next() {
        if (currentMediaIndex < mediaList.size - 1) {
            currentMediaIndex++
            play(mediaList[currentMediaIndex])
        }
    }

    private fun previous() {
        if (currentMediaIndex > 0) {
            currentMediaIndex--
            play(mediaList[currentMediaIndex])
        }
    }

    companion object {
        const val EXTRA_MEDIA_LIST = "extra_media_list"
        const val EXTRA_CURRENT_MEDIA_INDEX = "extra_current_media_index"
    }
}