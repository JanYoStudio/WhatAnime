package pw.janyo.whatanime.ui.activity

import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import pw.janyo.whatanime.R

import pw.janyo.whatanime.databinding.ActivityHistoryBinding
import pw.janyo.whatanime.databinding.ContentHistoryBinding
import pw.janyo.whatanime.handler.HistoryItemListener
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.HistoryRepository
import pw.janyo.whatanime.ui.adapter.HistoryRecyclerAdapter
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rxpackagedata.PackageData
import vip.mystery0.rxpackagedata.Status.*
import vip.mystery0.tools.base.binding.BaseBindingActivity

class HistoryActivity : BaseBindingActivity<ActivityHistoryBinding>(R.layout.activity_history) {
	private lateinit var contentHistoryBinding: ContentHistoryBinding
	private lateinit var historyViewModel: HistoryViewModel
	private lateinit var historyRecyclerAdapter: HistoryRecyclerAdapter

	private val animationHistoryObserver = Observer<PackageData<List<AnimationHistory>>> {
		when (it.status) {
			Content -> {
				historyRecyclerAdapter.items.clear()
				historyRecyclerAdapter.items.addAll(it.data!!)
				historyRecyclerAdapter.notifyDataSetChanged()
				dismissRefresh()
			}
			Loading -> showRefresh()
			Empty -> {
				dismissRefresh()
				Snackbar.make(binding.coordinatorLayout, R.string.hint_no_result, Snackbar.LENGTH_LONG)
						.show()
			}
			Error -> {
				Logs.wtf("animationHistoryObserver: ", it.error)
				dismissRefresh()
			}
		}
	}

	override fun inflateView(layoutId: Int) {
		binding = DataBindingUtil.setContentView(this, layoutId)
		contentHistoryBinding = binding.include
	}

	override fun initView() {
		super.initView()
		setSupportActionBar(binding.toolbar)
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		binding.toolbar.setNavigationOnClickListener {
			finish()
		}
		contentHistoryBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		historyRecyclerAdapter = HistoryRecyclerAdapter(this, HistoryItemListener(this, binding))
		contentHistoryBinding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
		contentHistoryBinding.recyclerView.adapter = historyRecyclerAdapter
		ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val position = viewHolder.adapterPosition
				historyRecyclerAdapter.notifyItemRemoved(position)
				HistoryRepository.deleteHistory(historyRecyclerAdapter.items.removeAt(position)) {
					Snackbar.make(binding.coordinatorLayout, if (it) R.string.hint_history_delete_done else R.string.hint_history_delete_error, Snackbar.LENGTH_LONG)
							.show()
					if (!it)
						showRefresh()
				}
			}
		}).attachToRecyclerView(contentHistoryBinding.recyclerView)
	}

	override fun initData() {
		super.initData()
		initViewModel()
		refresh()
	}

	private fun initViewModel() {
		historyViewModel = ViewModelProviders.of(this).get(HistoryViewModel::class.java)
		historyViewModel.historyList.observe(this, animationHistoryObserver)
	}

	override fun monitor() {
		super.monitor()
		contentHistoryBinding.swipeRefreshLayout.setOnRefreshListener {
			refresh()
		}
	}

	private fun refresh() {
		HistoryRepository.loadHistory(historyViewModel)
	}

	private fun showRefresh() {
		contentHistoryBinding.swipeRefreshLayout.isRefreshing = true
	}

	private fun dismissRefresh() {
		contentHistoryBinding.swipeRefreshLayout.isRefreshing = false
	}
}
