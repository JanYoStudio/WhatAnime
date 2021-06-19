package pw.janyo.whatanime.ui.activity

import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.databinding.ActivityHistoryBinding
import pw.janyo.whatanime.databinding.ContentHistoryBinding
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.adapter.HistoryRecyclerAdapter
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.tools.ResourceException

class HistoryActivity : WABaseActivity<ActivityHistoryBinding>(R.layout.activity_history),
    AndroidScopeComponent {
    override val scope: Scope by activityScope()

    private lateinit var contentHistoryBinding: ContentHistoryBinding
    private val historyViewModel: HistoryViewModel by viewModel()
    private val historyRecyclerAdapter: HistoryRecyclerAdapter by inject()
    private val adsDialog: AlertDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_why_ad)
            .setMessage(R.string.hint_why_ads_appear)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

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
                Logger.wtf("animationHistoryObserver: ", e)
            dismissRefresh()
            Snackbar.make(
                binding.coordinatorLayout, e?.message
                    ?: "", Snackbar.LENGTH_LONG
            )
                .show()
        }
    }

    override fun inflateView(layoutId: Int) {
        super.inflateView(layoutId)
        contentHistoryBinding = binding.include
    }

    override fun initView() {
        super.initView()
        if (inBlackList) {
            //连接上了服务器并且在黑名单中
            contentHistoryBinding.adView.visibility = View.VISIBLE
            contentHistoryBinding.whyAdImageView.visibility = View.VISIBLE
            //初始化AdMod
            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            contentHistoryBinding.adView.loadAd(adRequest)
        } else {
            contentHistoryBinding.adView.visibility = View.GONE
            contentHistoryBinding.whyAdImageView.visibility = View.GONE
        }
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.title_activity_history)
        binding.toolbar.title = title
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        contentHistoryBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        contentHistoryBinding.recyclerView.adapter = historyRecyclerAdapter
        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                historyRecyclerAdapter.notifyItemRemoved(position)
                historyViewModel.deleteHistory(historyRecyclerAdapter.items.removeAt(position)) {
                    Snackbar.make(
                        binding.coordinatorLayout,
                        if (it) R.string.hint_history_delete_done else R.string.hint_history_delete_error,
                        Snackbar.LENGTH_SHORT
                    )
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
        contentHistoryBinding.whyAdImageView.setOnClickListener {
            adsDialog.show()
        }
        contentHistoryBinding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                contentHistoryBinding.adView.visibility = View.VISIBLE
                contentHistoryBinding.whyAdImageView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                contentHistoryBinding.adView.visibility = View.GONE
                contentHistoryBinding.whyAdImageView.visibility = View.GONE
            }
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
