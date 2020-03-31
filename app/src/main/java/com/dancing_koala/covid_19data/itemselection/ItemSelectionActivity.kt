package com.dancing_koala.covid_19data.itemselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.dancing_koala.covid_19data.R
import kotlinx.android.synthetic.main.activity_item_selection.*

class ItemSelectionActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ITEM_SELECTED = "ItemSelectionActivity.extra.selecte_item"
    }

    private val viewModel: ItemSelectionViewModel by viewModels()
    private val selectableItemAdapter = SelectableItemAdapter { id -> onItemSelected(id) }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_selection)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.item_selection_screen_title)
        }

        itemSelectionRecyclerView.apply {
            adapter = selectableItemAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        itemSelectionSearchField.apply {
            setOnCloseListener {
                viewModel.onClearSearch()
                true
            }

            val onQueryTextListener = object : OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onNewSearchQuery(newText)
                    return true
                }
            }

            this.setOnQueryTextListener(onQueryTextListener)
        }

        viewModel.viewStateLiveData.observe(this, Observer { viewState ->
            when (viewState) {
                is ViewState.UpdateItems   -> selectableItemAdapter.updateItems(viewState.items)
                is ViewState.FinishAsOk    -> setResultAndFinish(viewState.selectItemId)
                ViewState.FinishAsCanceled -> cancel()
            }
        })

        viewModel.start()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            viewModel.onBackClick()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onItemSelected(id: Int) = viewModel.onItemSelected(id)

    private fun setResultAndFinish(selectedItemId: Int) {
        setResult(Activity.RESULT_OK, Intent().putExtra(EXTRA_ITEM_SELECTED, selectedItemId))
        finish()
    }

    private fun cancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}