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
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var adapter: HistoryAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        database = AppDatabase.getDatabase(this)
        setupToolbar()
        setupRecyclerView()
        setupClearButton()
        observeHistory()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbarHistory)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupRecyclerView() {
        val rvHistory = findViewById<RecyclerView>(R.id.rvHistory)
        adapter = HistoryAdapter()
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter
    }

    private fun setupClearButton() {
        val btnClear = findViewById<Button>(R.id.btnClearHistory)
        btnClear.setOnClickListener {
            lifecycleScope.launch {
                database.historyDao().clearAllHistory()
            }
        }
    }

    private fun observeHistory() {
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyHistory)
        lifecycleScope.launch {
            database.historyDao().getAllHistory().collect { historyList ->
                if (historyList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                }
                adapter.submitList(historyList)
            }
        }
    }
}
