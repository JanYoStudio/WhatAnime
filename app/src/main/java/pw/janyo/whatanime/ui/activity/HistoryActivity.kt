package pw.janyo.whatanime.ui.activity

import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.gms.ads.*
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.BaseComposeActivity
import pw.janyo.whatanime.config.inBlackList
import pw.janyo.whatanime.model.Animation
import pw.janyo.whatanime.model.AnimationHistory
import pw.janyo.whatanime.ui.theme.MyApplicationTheme
import pw.janyo.whatanime.ui.theme.observeValueAsState
import pw.janyo.whatanime.viewModel.HistoryViewModel
import vip.mystery0.tools.factory.fromJson
import vip.mystery0.tools.utils.getCalendarFromLong
import vip.mystery0.tools.utils.toDateTimeString
import java.io.File
import java.text.DecimalFormat

class HistoryActivity : BaseComposeActivity<HistoryViewModel>(), AndroidScopeComponent {

    override val scope: Scope by activityScope()

    override val viewModel: HistoryViewModel by viewModel()

    @Composable
    override fun BuildContent() {
        MyApplicationTheme {
            BuildAppBar {
                if (inBlackList) {
                    BuildAdLayout()
                }
                val isRefreshing by viewModel.refreshData.observeValueAsState()
                val historyList by viewModel.historyList.observeAsState()
                val errorMessage by viewModel.errorMessageData.observeAsState()
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing),
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    BuildList(historyList)
                    BuiltSnackbar(text = errorMessage)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.refresh()
    }

    @Composable
    fun BuildAdLayout() {
        var adLoadResult by remember {
            mutableStateOf(true)
        }
        //初始化AdMod
        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()
        if (!adLoadResult) {
            return
        }
        val showAdsDialog = remember { mutableStateOf(false) }
        if (showAdsDialog.value) {
            AlertDialog(onDismissRequest = {
                showAdsDialog.value = false
            }, title = {
                Text(text = stringResource(id = R.string.action_why_ad))
            }, text = {
                Text(text = stringResource(id = R.string.hint_why_ads_appear))
            }, confirmButton = {})
        }
        Row {
            AndroidView(
                modifier = Modifier.fillMaxWidth(), // Occupy the max size in the Compose UI tree
                factory = { context ->
                    AdView(context).apply {
                        this.adSize = AdSize.BANNER
                        this.adUnitId = "ca-app-pub-6114262658640635/9315758560"
                        loadAd(adRequest)
                        this.adListener = object : AdListener() {
                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                adLoadResult = false
                            }
                        }
                    }
                }
            )
            IconButton(onClick = {
                showAdsDialog.value = true
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_why_show_ad),
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    fun BuildList(list: List<AnimationHistory>?) {
        if (list == null) {
            return
        }
        if (list.isEmpty()) {
            viewModel.errorMessageState(stringResource(id = R.string.hint_no_result))
            return
        }
        LazyColumn(
            modifier = Modifier.fillMaxHeight()
        ) {
            list.forEach {
                item {
                    Card(
                        modifier = Modifier
                            .padding(8.dp),
                        border = BorderStroke(
                            1.dp,
                            colorResource(id = R.color.outlined_stroke_color)
                        ),
                        elevation = 0.dp
                    ) {
                        BuildItem(history = it, animation = it.result.fromJson())
                    }
                }
            }
        }
    }

    @Composable
    fun BuildItem(history: AnimationHistory, animation: Animation) {
        val animationDocs = if (animation.docs.isNotEmpty()) animation.docs[0] else null
        val similarity = if (animationDocs == null)
            "0%"
        else
            "${DecimalFormat("#.0000").format(animationDocs.similarity * 100)}%"
        Row(modifier = Modifier.padding(8.dp)) {
            Image(
                painter = rememberCoilPainter(request = File(history.cachePath)),
                contentDescription = null,
                modifier = Modifier
                    .height(90.dp)
                    .width(160.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                BuildText(text = stringResource(id = R.string.hint_time_history))
                BuildText(text = stringResource(id = R.string.hint_title_native))
                BuildText(text = stringResource(id = R.string.hint_title_chinese))
                BuildText(text = stringResource(id = R.string.hint_episode))
                BuildText(text = stringResource(id = R.string.hint_similarity))
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                BuildText(history.time.getCalendarFromLong().toDateTimeString())
                BuildText(animationDocs?.title_native ?: "")
                BuildText(animationDocs?.title_chinese ?: "")
                BuildText(animationDocs?.episode ?: "")
                BuildText(similarity)
            }
        }
    }

    @Composable
    fun BuildText(text: String) {
        Text(text = text, fontSize = 12.sp, maxLines = 1)
    }

    @Composable
    fun BuiltSnackbar(text: String?) {
        text?.let {
            Snackbar { Text(text = text) }
        }
    }
}

//class HistoryActivity1 : WABaseActivity<ActivityHistoryBinding>(R.layout.activity_history),
//    AndroidScopeComponent {
//    override val scope: Scope by activityScope()
//
//    private lateinit var contentHistoryBinding: ContentHistoryBinding
//    private val historyViewModel: HistoryViewModel by viewModel()
//    private val historyRecyclerAdapter: HistoryRecyclerAdapter by inject()
//    private val adsDialog: AlertDialog by lazy {
//        MaterialAlertDialogBuilder(this)
//            .setTitle(R.string.action_why_ad)
//            .setMessage(R.string.hint_why_ads_appear)
//            .setPositiveButton(android.R.string.ok, null)
//            .create()
//    }
//
//    private val animationHistoryObserver = object : PackageDataObserver<List<AnimationHistory>> {
//        override fun content(data: List<AnimationHistory>?) {
//            historyRecyclerAdapter.items.clear()
//            historyRecyclerAdapter.items.addAll(data!!)
//            dismissRefresh()
//        }
//
//        override fun loading() {
//            showRefresh()
//        }
//
//        override fun empty(data: List<AnimationHistory>?) {
//            dismissRefresh()
//            Snackbar.make(binding.coordinatorLayout, R.string.hint_no_result, Snackbar.LENGTH_LONG)
//                .show()
//        }
//
//        override fun error(data: List<AnimationHistory>?, e: Throwable?) {
//            if (e !is ResourceException)
//                Logger.wtf("animationHistoryObserver: ", e)
//            dismissRefresh()
//            Snackbar.make(
//                binding.coordinatorLayout, e?.message
//                    ?: "", Snackbar.LENGTH_LONG
//            )
//                .show()
//        }
//    }
//
//    override fun inflateView(layoutId: Int) {
//        super.inflateView(layoutId)
//        contentHistoryBinding = binding.include
//    }
//
//    override fun initView() {
//        super.initView()
//        if (inBlackList) {
//            //连接上了服务器并且在黑名单中
//            contentHistoryBinding.adView.visibility = View.VISIBLE
//            contentHistoryBinding.whyAdImageView.visibility = View.VISIBLE
//            //初始化AdMod
//            MobileAds.initialize(this) {}
//            val adRequest = AdRequest.Builder().build()
//            contentHistoryBinding.adView.loadAd(adRequest)
//        } else {
//            contentHistoryBinding.adView.visibility = View.GONE
//            contentHistoryBinding.whyAdImageView.visibility = View.GONE
//        }
//        setSupportActionBar(binding.toolbar)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        title = getString(R.string.title_activity_history)
//        binding.toolbar.title = title
//        binding.toolbar.setNavigationOnClickListener {
//            finish()
//        }
//        contentHistoryBinding.recyclerView.layoutManager = LinearLayoutManager(this)
//        contentHistoryBinding.recyclerView.adapter = historyRecyclerAdapter
//        ItemTouchHelper(object :
//            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//            override fun onMove(
//                recyclerView: RecyclerView,
//                viewHolder: RecyclerView.ViewHolder,
//                target: RecyclerView.ViewHolder
//            ): Boolean {
//                return false
//            }
//
//            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val position = viewHolder.adapterPosition
//                historyRecyclerAdapter.notifyItemRemoved(position)
//                historyViewModel.deleteHistory(historyRecyclerAdapter.items.removeAt(position)) {
//                    Snackbar.make(
//                        binding.coordinatorLayout,
//                        if (it) R.string.hint_history_delete_done else R.string.hint_history_delete_error,
//                        Snackbar.LENGTH_SHORT
//                    )
//                        .show()
//                    if (!it)
//                        showRefresh()
//                }
//            }
//        }).attachToRecyclerView(contentHistoryBinding.recyclerView)
//    }
//
//    override fun initData() {
//        super.initData()
//        initViewModel()
//        refresh()
//    }
//
//    private fun initViewModel() {
////        historyViewModel.historyList.observe(this, animationHistoryObserver)
//    }
//
//    override fun monitor() {
//        super.monitor()
//        contentHistoryBinding.swipeRefreshLayout.setOnRefreshListener {
//            refresh()
//        }
//        contentHistoryBinding.whyAdImageView.setOnClickListener {
//            adsDialog.show()
//        }
//        contentHistoryBinding.adView.adListener = object : AdListener() {
//            override fun onAdLoaded() {
//                contentHistoryBinding.adView.visibility = View.VISIBLE
//                contentHistoryBinding.whyAdImageView.visibility = View.VISIBLE
//            }
//
//            override fun onAdFailedToLoad(p0: LoadAdError) {
//                contentHistoryBinding.adView.visibility = View.GONE
//                contentHistoryBinding.whyAdImageView.visibility = View.GONE
//            }
//        }
//    }
//
//    private fun refresh() {
//        historyViewModel.refresh()
//    }
//
//    private fun showRefresh() {
//        contentHistoryBinding.swipeRefreshLayout.isRefreshing = true
//    }
//
//    private fun dismissRefresh() {
//        contentHistoryBinding.swipeRefreshLayout.isRefreshing = false
//    }
//}
