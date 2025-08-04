package com.example.chan

import android.os.Bundle
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        loadingSpinner = findViewById(R.id.loading_spinner)
        
        if (savedInstanceState == null) {
            val fragment = MainFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_frame, fragment)
                .commitNow()
        }
    }
}