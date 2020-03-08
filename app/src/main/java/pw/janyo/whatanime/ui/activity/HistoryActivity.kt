package pw.janyo.whatanime.ui.activity

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.scope.lifecycleScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.databinding.ActivityHistoryBinding
import pw.janyo.whatanime.databinding.ContentHistoryBinding
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.adapter.HistoryRecyclerAdapter
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.logs.Logs
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.tools.ResourceException

class HistoryActivity : WABaseActivity<ActivityHistoryBinding>(R.layout.activity_history) {
	private lateinit var contentHistoryBinding: ContentHistoryBinding
	private val historyViewModel: HistoryViewModel by viewModel()
	private val historyRecyclerAdapter: HistoryRecyclerAdapter by lifecycleScope.inject { parametersOf(this) }

	private val animationHistoryObserver = object : PackageDataObserver<List<AnimationHistory>> {
		override fun content(data: List<AnimationHistory>?) {
			historyRecyclerAdapter.items.clear()
			historyRecyclerAdapter.items.addAll(data!!)
			dismissRefresh()
		}

		override fun loading() {
			showRefresh()
		}

		override fun empty(data: List<AnimationHistory>?) {
			dismissRefresh()
			Snackbar.make(binding.coordinatorLayout, R.string.hint_no_result, Snackbar.LENGTH_LONG)
					.show()
		}

		override fun error(data: List<AnimationHistory>?, e: Throwable?) {
			if (e !is ResourceException)
				Logs.wtf("animationHistoryObserver: ", e)
			dismissRefresh()
			Snackbar.make(binding.coordinatorLayout, e?.message
							?: "", Snackbar.LENGTH_LONG)
					.show()
		}
	}

	override fun inflateView(layoutId: Int) {
		super.inflateView(layoutId)
		contentHistoryBinding = binding.include
	}

	override fun initView() {
		super.initView()
		setSupportActionBar(binding.toolbar)
		supportActionBar!!.setDisplayHomeAsUpEnabled(true)
		title = getString(R.string.title_activity_history)
		binding.toolbar.title = title
		binding.toolbar.setNavigationOnClickListener {
			finish()
		}
		contentHistoryBinding.recyclerView.layoutManager = LinearLayoutManager(this)
		contentHistoryBinding.recyclerView.adapter = historyRecyclerAdapter
		ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
			override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
				return false
			}

			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val position = viewHolder.adapterPosition
				historyRecyclerAdapter.notifyItemRemoved(position)
				historyViewModel.deleteHistory(historyRecyclerAdapter.items.removeAt(position)) {
					Snackbar.make(binding.coordinatorLayout, if (it) R.string.hint_history_delete_done else R.string.hint_history_delete_error, Snackbar.LENGTH_SHORT)
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
		historyViewModel.historyList.observe(this, animationHistoryObserver)
	}

	override fun monitor() {
		super.monitor()
		contentHistoryBinding.swipeRefreshLayout.setOnRefreshListener {
			refresh()
		}
	}

	private fun refresh() {
		historyViewModel.loadHistory()
	}

	private fun showRefresh() {
		contentHistoryBinding.swipeRefreshLayout.isRefreshing = true
	}

	private fun dismissRefresh() {
		contentHistoryBinding.swipeRefreshLayout.isRefreshing = false
	}
}
