package com.example.translateapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.translateapp.MainActivity
import com.example.translateapp.R
import com.example.translateapp.data.AppDatabase
import com.example.translateapp.model.SharedTranslationViewModel
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var adapter: HistoryAdapter
    private lateinit var database: AppDatabase

    private lateinit var toolbar: Toolbar
    private lateinit var rvHistory: RecyclerView
    private lateinit var btnClear: Button
    private lateinit var tvEmpty: TextView

    private lateinit var sharedViewModel: SharedTranslationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_history,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        bindViews(view)
        sharedViewModel =
            ViewModelProvider(requireActivity())
                .get(SharedTranslationViewModel::class.java)

        setupToolbar()
        setupRecyclerView()
        setupClearButton()
        observeHistory()
    }

    private fun bindViews(view: View) {
        toolbar = view.findViewById(R.id.toolbarHistory)
        rvHistory = view.findViewById(R.id.rvHistory)
        btnClear = view.findViewById(R.id.btnClearHistory)
        tvEmpty = view.findViewById(R.id.tvEmptyHistory)
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
        adapter = HistoryAdapter { history ->

            sharedViewModel.setTranslation(
                history.sourceText,
                history.translatedText,
                history.sourceLang,
                history.targetLang
            )

            (activity as MainActivity).selectHomeTab()
        }

        rvHistory.layoutManager =
            LinearLayoutManager(requireContext())

        rvHistory.adapter = adapter
    }

    private fun setupClearButton() {
        btnClear.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                database.historyDao()
                    .clearAllHistory()
            }
        }
    }

    private fun observeHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            database.historyDao()
                .getAllHistory()
                .collect { historyList ->

                    tvEmpty.visibility =
                        if (historyList.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE

                    adapter.submitList(historyList)
                }
        }
    }
}