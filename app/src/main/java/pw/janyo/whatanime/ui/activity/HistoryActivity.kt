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
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.repository.HistoryRepository
import pw.janyo.whatanime.ui.adapter.HistoryRecyclerAdapter
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.tools.base.BaseActivity

class HistoryActivity : BaseActivity(R.layout.activity_history) {
	private lateinit var activityHistoryBinding: ActivityHistoryBinding
	private lateinit var contentHistoryBinding: ContentHistoryBinding
	private lateinit var historyViewModel: HistoryViewModel
	private lateinit var historyRecyclerAdapter: HistoryRecyclerAdapter
	private val animationHistoryList = ArrayList<AnimationHistory>()

	private val animationHistoryObserver = Observer<List<AnimationHistory>> {
		animationHistoryList.clear()
		animationHistoryList.addAll(it)
		historyRecyclerAdapter.notifyDataSetChanged()
		dismissRefresh()
	}
	private val messageObserver = Observer<String> {
		dismissRefresh()
		Snackbar.make(activityHistoryBinding.coordinatorLayout, it, Snackbar.LENGTH_LONG)
				.show()
	}

	override fun inflateView(layoutId: Int) {
		activityHistoryBinding = DataBindingUtil.setContentView(this, layoutId)
		contentHistoryBinding = activityHistoryBinding.include
	}

	override fun initView() {
		super.initView()
		setSupportActionBar(activityHistoryBinding.toolbar)
		supportActionBar!!.setDisplayShowHomeEnabled(true)
		activityHistoryBinding.toolbar.setNavigationOnClickListener {
			finish()
		}
		contentHistoryBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		historyRecyclerAdapter = HistoryRecyclerAdapter(this, activityHistoryBinding, animationHistoryList)
		contentHistoryBinding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
		contentHistoryBinding.recyclerView.adapter = historyRecyclerAdapter
		ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val position = viewHolder.adapterPosition
				historyRecyclerAdapter.notifyItemRemoved(position)
				HistoryRepository.deleteHistory(animationHistoryList.removeAt(position), historyViewModel)
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
		historyViewModel.message.observe(this, messageObserver)
	}

	override fun monitor() {
		super.monitor()
		contentHistoryBinding.swipeRefreshLayout.setOnRefreshListener {
			refresh()
		}
	}

	private fun refresh() {
		showRefresh()
		HistoryRepository.loadHistory(historyViewModel)
	}

	private fun showRefresh() {
		contentHistoryBinding.swipeRefreshLayout.isRefreshing = true
	}

	private fun dismissRefresh() {
		contentHistoryBinding.swipeRefreshLayout.isRefreshing = false
	}
}
