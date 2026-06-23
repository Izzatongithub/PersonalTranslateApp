package com.example.translateapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.translateapp.R
import com.example.translateapp.data.AppDatabase
import com.example.translateapp.model.FavoriteEntity
import kotlinx.coroutines.launch

class FavoriteActivity : AppCompatActivity() {
    private lateinit var adapter: FavoriteAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        database = AppDatabase.getDatabase(this)
        setupToolbar()
        setupRecyclerView()
        observeFavorite()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarFavorite)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        val rvFavorite = findViewById<RecyclerView>(R.id.rvFavorite)
        adapter = FavoriteAdapter { favorite ->
            lifecycleScope.launch {
                database.favoriteDao().deleteFavoriteById(favorite.idFavorite)
            }
        }
        rvFavorite.layoutManager = LinearLayoutManager(this)
        rvFavorite.adapter = adapter
    }

    private fun observeFavorite() {
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyFavorite)
        lifecycleScope.launch {
            database.favoriteDao().getAllFavorites().collect { favoriteList ->
                if (favoriteList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                }
                adapter.setData(favoriteList)
            }
        }
    }
}