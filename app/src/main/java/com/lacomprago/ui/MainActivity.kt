package com.lacomprago.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.lacomprago.R

/**
 * Main Activity - Entry point of the application
 * Will handle token input and navigation to product list
 */
class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
