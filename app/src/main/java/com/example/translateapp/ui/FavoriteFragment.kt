package com.example.translateapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.translateapp.R
import com.example.translateapp.data.AppDatabase
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private lateinit var adapter: FavoriteAdapter
    private lateinit var database: AppDatabase

    private lateinit var rvFavorite: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_favorite,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        toolbar = view.findViewById(R.id.toolbarFavorite)
        rvFavorite = view.findViewById(R.id.rvFavorite)
        tvEmpty = view.findViewById(R.id.tvEmptyFavorite)

        setupToolbar()
        setupRecyclerView()
        observeFavorite()
    }

    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity)
            .setSupportActionBar(toolbar)

        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        adapter = FavoriteAdapter { favorite ->
            lifecycleScope.launch {
                database.favoriteDao()
                    .deleteFavoriteById(favorite.idFavorite)
            }
        }

        rvFavorite.layoutManager =
            LinearLayoutManager(requireContext())

        rvFavorite.adapter = adapter
    }

    private fun observeFavorite() {
        lifecycleScope.launch {
            database.favoriteDao()
                .getAllFavorites()
                .collect { favoriteList ->

                    tvEmpty.visibility =
                        if (favoriteList.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE

                    adapter.setData(favoriteList)
                }
        }
    }
}