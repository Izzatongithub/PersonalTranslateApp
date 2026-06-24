package com.example.translateapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.translateapp.ui.DictionaryFragment
import com.example.translateapp.ui.FavoriteFragment
import com.example.translateapp.ui.HistoryFragment
import com.example.translateapp.ui.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadFragment(HomeFragment())  // default

        findViewById<BottomNavigationView>(R.id.bottomNav)
            .setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.homeFragment    -> loadFragment(HomeFragment())
                    R.id.smartDictionaryFragment    -> loadFragment(DictionaryFragment())
                    R.id.favoritesFragment -> loadFragment(FavoriteFragment())
                    R.id.historyFragment -> loadFragment(HistoryFragment())
                }
                true
            }
    }

    // Helper: ganti Fragment di dalam container
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()                    // mulai transaksi
            .replace(R.id.fragmentContainer, fragment) // ganti isi container
            .commit()                              // jalankan
    }

}