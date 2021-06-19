package pw.janyo.whatanime.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
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
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import pw.janyo.whatanime.R
import pw.janyo.whatanime.base.WABaseActivity
import pw.janyo.whatanime.config.*
import pw.janyo.whatanime.databinding.ActivityMainBinding
import pw.janyo.whatanime.databinding.ContentMainBinding
import pw.janyo.whatanime.model.Docs
import pw.janyo.whatanime.model.SearchQuota
import pw.janyo.whatanime.model.ShowImage
import pw.janyo.whatanime.ui.adapter.MainRecyclerAdapter
import pw.janyo.whatanime.utils.loadWithoutCache
import pw.janyo.whatanime.viewModel.MainViewModel
import vip.mystery0.rx.DataObserver
import vip.mystery0.rx.PackageDataObserver
import vip.mystery0.rx.content
import vip.mystery0.tools.ResourceException
import vip.mystery0.tools.utils.formatTime
import java.io.File
import kotlin.system.exitProcess

//class MainActivity : BaseComponentActivity() {
//    private val mainViewModel: MainViewModel by viewModel()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MyApplicationTheme {
//                BuildContent()
//            }
//        }
//    }
//
//    @Composable
//    fun BuildContent() {
//        val resultList = mainViewModel.resultList.observeAsState()
//        Column {
//            Image(painter = painterResource(id = R.mipmap.janyo_studio), contentDescription = null)
//            BuildList(resultList = resultList)
//        }
//    }
//
//    @Composable
//    fun BuildList(resultList: State<List<Docs>?>) {
//        Column {
//            resultList.value?.forEach {
//                BuildResultItem(docs = it)
//            }
//        }
//    }
//
//    @Composable
//    fun BuildResultItem(docs: Docs) {
//        val requestUrl = Constant.previewUrl.replace("{anilist_id}", docs.anilist_id.toString())
//            .replace("{fileName}", Uri.encode(docs.filename))
//            .replace("{at}", docs.at.toString())
//            .replace("{token}", docs.tokenthumb ?: "")
//
//        Column {
//            if (docs.similarity < 0.87) {
//                Row(horizontalArrangement = Arrangement.End) {
//                    Text(text = stringResource(id = R.string.hint_probably_mistake))
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_probably_mistake),
//                        contentDescription = null
//                    )
//                }
//            }
//            Text(text = "${stringResource(id = R.string.hint_title_native)}${docs.title_native ?: ""}")
//            Text(text = "${stringResource(id = R.string.hint_title_chinese)}${docs.title_chinese ?: ""}")
//            Text(text = "${stringResource(id = R.string.hint_title_english)}${docs.title_english ?: ""}")
//            Text(text = "${stringResource(id = R.string.hint_title_romaji)}${docs.title_romaji ?: ""}")
//            Text(text = "${stringResource(id = R.string.hint_title_native)}${docs.title_native ?: ""}")
//            Row {
//                Image(painter = rememberCoilPainter(request = requestUrl), contentDescription = null)
//                Column {
//                    Text(text = "${stringResource(id = R.string.hint_time)}${(docs.at.toLong() * 1000).formatTime()}")
//                    Text(text = "${stringResource(id = R.string.hint_episode)}${docs.episode}")
//                    Text(text = "${stringResource(id = R.string.hint_ani_list_id)}${docs.anilist_id}")
//                    Text(text = "${stringResource(id = R.string.hint_mal_id)}${docs.mal_id}")
//                    Text(text = "${stringResource(id = R.string.hint_similarity)}${"${DecimalFormat("#.000").format(docs.similarity * 100)}%"}")
//                }
//            }
//        }
//    }
//}

class MainActivity : WABaseActivity<ActivityMainBinding>(R.layout.activity_main),
    AndroidScopeComponent {
    override val scope: Scope by activityScope()

    companion object {
        private const val INTENT_CACHE_FILE = "INTENT_CACHE_FILE"
        private const val INTENT_ORIGIN_PATH = "INTENT_ORIGIN_PATH"
        private const val INTENT_TITLE = "INTENT_TITLE"
        private const val INTENT_URI = "INTENT_URI"
        private const val INTENT_MIME_TYPE = "INTENT_MIME_TYPE"

        fun showDetail(context: Context, cacheFile: File, originPath: String, title: String) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_CACHE_FILE, cacheFile)
            intent.putExtra(INTENT_ORIGIN_PATH, originPath)
            intent.putExtra(INTENT_TITLE, title)
            context.startActivity(intent)
        }

        fun receiveShare(context: Context, uri: Uri, mimeType: String) {
            Logger.i("receiveShare: uri: $uri, mimeType: $mimeType")
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_URI, uri)
            intent.putExtra(INTENT_MIME_TYPE, mimeType)
            context.startActivity(intent)
        }
    }

    private lateinit var contentMainBinding: ContentMainBinding
    private val mainViewModel: MainViewModel by viewModel()
    private val player: ExoPlayer by inject { parametersOf(this) }
    private val mainRecyclerAdapter: MainRecyclerAdapter by inject { parametersOf(this) }
    private var isShowDetail = false
    private val dialog: Dialog by lazy { buildZLoadingDialog().create() }
    private val adsDialog: AlertDialog by lazy {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_why_ad)
            .setMessage(R.string.hint_why_ads_appear)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }
    private val selectIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val type = contentResolver.getType(it.data!!.data!!)
                if (type.isNullOrBlank()) {
                    toast(R.string.hint_select_file_not_exist)
                } else {
                    mainViewModel.parseImageFile(it.data!!, type)
                }
            }
        }

    private val quotaObserver = object : DataObserver<SearchQuota> {
        override fun contentNoEmpty(data: SearchQuota) {
            val limit = if (data.limit == 0) data.user_limit else data.limit
            val refreshTime = if (data.limit_ttl == 0) data.user_limit_ttl else data.limit_ttl
            val quotaString =
                "${getString(R.string.hint_search_quota)}${limit}    ${getString(R.string.hint_search_quota_ttl)}${
                    (refreshTime * 1000).toLong().formatTime()
                }"
            contentMainBinding.searchQuota.text = quotaString
        }

        override fun error(e: Throwable?) {
            if (e !is ResourceException)
                Logger.wtf("quotaObserver: ", e)
            e.toastLong()
        }
    }

    private val animationObserver = object : DataObserver<List<Docs>> {
        override fun contentNoEmpty(data: List<Docs>) {
            mainRecyclerAdapter.items.clear()
            mainRecyclerAdapter.items.addAll(data)
            hideDialog()
        }

        override fun loading() {
            showDialog()
        }

        override fun empty() {
            hideDialog()
            Snackbar.make(binding.coordinatorLayout, R.string.hint_no_result, Snackbar.LENGTH_SHORT)
                .show()
        }

        override fun error(e: Throwable?) {
            if (e !is ResourceException)
                Logger.wtf("animationObserver: ", e)
            hideDialog()
            e.toastLong()
        }
    }
    private val imageFileObserver = object : DataObserver<ShowImage> {
        override fun contentNoEmpty(data: ShowImage) {
            val originFile = File(data.originPath)
            //判断图片文件是否存在
            if (!originFile.exists()) {
                //如果不存在，显示错误信息
                Snackbar.make(
                    binding.coordinatorLayout,
                    R.string.hint_select_file_not_exist,
                    Snackbar.LENGTH_LONG
                )
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                            finish()
                        }
                    })
                    .show()
                return
            }
            //图片存在，加载图片显示
            contentMainBinding.imageView.loadWithoutCache(originFile)
            //搜索图片
            mainViewModel.search(
                originFile,
                null,
                data.cachePath,
                data.originPath,
                data.mimeType,
                connectServer
            )
        }

        override fun error(e: Throwable?) {
            if (e !is ResourceException)
                Logger.wtf("imageFileObserver: ", e)
            e.toastLong()
        }
    }
    private val mediaSourceObserver = object : PackageDataObserver<MediaSource> {
        override fun content(data: MediaSource?) {
            if (data == null) {
                //再次播放当前视频
                if (!player.isPlaying)
                    player.seekToDefaultPosition()
            } else {
                //播放新的视频
                player.clearMediaItems()
                player.setMediaSource(data)
                player.prepare()
                player.playWhenReady = true
            }
        }

        override fun error(data: MediaSource?, e: Throwable?) {
            if (e !is ResourceException)
                Logger.wtf("imageFileObserver: ", e)
            e.toastLong()
        }
    }

    override fun inflateView(layoutId: Int) {
        super.inflateView(layoutId)
        contentMainBinding = binding.include
    }

    override fun initView() {
        super.initView()
        if (inBlackList) {
            //连接上了服务器并且在黑名单中
            contentMainBinding.adView.visibility = View.VISIBLE
            contentMainBinding.whyAdImageView.visibility = View.VISIBLE
            //初始化AdMod
            MobileAds.initialize(this) {}
            val adRequest = AdRequest.Builder().build()
            contentMainBinding.adView.loadAd(adRequest)
        } else {
            contentMainBinding.adView.visibility = View.GONE
            contentMainBinding.whyAdImageView.visibility = View.GONE
        }
        title = getString(R.string.title_activity_main)
        setSupportActionBar(binding.toolbar)
        contentMainBinding.recyclerView.layoutManager = LinearLayoutManager(this)
        contentMainBinding.recyclerView.adapter = mainRecyclerAdapter
    }

    override fun initData() {
        super.initData()
        initViewModel()
        mainViewModel.showQuota()
        initIntent()
        contentMainBinding.videoView.player = player
        showNotice()
    }

    private fun initViewModel() {
        mainViewModel.quota.observe(this, quotaObserver)
        mainViewModel.imageFile.observe(this, imageFileObserver)
        mainViewModel.resultList.observe(this, animationObserver)
        mainViewModel.isShowDetail.observe(this, { isShowDetail = it })
        mainViewModel.mediaSource.observe(this, mediaSourceObserver)
    }

    @SuppressLint("RestrictedApi")
    private fun initIntent() {
        if (intent.hasExtra(INTENT_URI)) {
            //接收其他来源的图片
            try {
                val uri = intent.getParcelableExtra<Uri>(INTENT_URI)
                intent.data = uri
                mainViewModel.parseImageFile(intent, intent.getStringExtra(INTENT_MIME_TYPE)!!)
            } catch (e: Exception) {
                getString(R.string.hint_select_file_path_null).toast()
            }
        }
        if (intent.hasExtra(INTENT_CACHE_FILE) && intent.hasExtra(INTENT_TITLE)) {
            //查看历史记录
            mainViewModel.isShowDetail.value = true
            binding.fab.visibility = View.GONE
            val cacheFile: File = intent.getSerializableExtra(INTENT_CACHE_FILE) as File
            val originPath = intent.getStringExtra(INTENT_ORIGIN_PATH)!!
            //加载显示历史记录中的缓存文件
            val showImage = ShowImage()
            showImage.mimeType = ""
            showImage.originPath = originPath
            showImage.cachePath = cacheFile.absolutePath
            mainViewModel.imageFile.content(showImage)
            //设置标题
            title = intent.getStringExtra(INTENT_TITLE)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            binding.toolbar.setNavigationOnClickListener {
                finish()
            }
        }
    }

    override fun monitor() {
        super.monitor()
        binding.fab.setOnClickListener {
            doSelect()
        }
        contentMainBinding.whyAdImageView.setOnClickListener {
            adsDialog.show()
        }
        contentMainBinding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                contentMainBinding.adView.visibility = View.VISIBLE
                contentMainBinding.whyAdImageView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                contentMainBinding.adView.visibility = View.GONE
                contentMainBinding.whyAdImageView.visibility = View.GONE
            }
        }
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                when (state) {
                    Player.STATE_BUFFERING -> {
                        contentMainBinding.imageView.visibility = View.GONE
                        contentMainBinding.videoView.visibility = View.VISIBLE
                        contentMainBinding.progressBar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        contentMainBinding.progressBar.visibility = View.GONE
                    }
                    Player.STATE_ENDED -> {
                        contentMainBinding.videoView.visibility = View.GONE
                        contentMainBinding.imageView.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                super.onPlayerError(error)
                Logger.e("onPlayerError: ", error)
                contentMainBinding.progressBar.visibility = View.GONE
                contentMainBinding.videoView.visibility = View.GONE
                contentMainBinding.imageView.visibility = View.VISIBLE
                error.toastLong()
            }
        })
    }

    private fun showNotice() {
        if (intent.hasExtra(INTENT_URI) || intent.hasExtra(INTENT_CACHE_FILE)) return
        if (Configure.alreadyReadNotice) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_usage_notice)
            .setMessage(R.string.hint_usage_notice)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                Configure.alreadyReadNotice = true
                dialog.dismiss()
            }
            .setNegativeButton(R.string.action_disagree) { _, _ ->
                Configure.alreadyReadNotice = false
                finish()
                exitProcess(0)
            }
            .show()
    }

    private fun doSelect() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        selectIntent.launch(intent)
    }

    private fun showDialog() {
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun hideDialog() {
        if (dialog.isShowing)
            dialog.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (!isShowDetail) {
            menuInflater.inflate(R.menu.menu_main, menu)
            menu!!.findItem(R.id.action_why_ad).isVisible = inBlackList
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            R.id.action_info -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            R.id.action_about -> {
                toCustomTabs(if (inChina == true) "https://janyostudio.mystery0.vip/wa/index.html" else "https://janyostudio.mystery0.app/wa/index.html")
                true
            }
            R.id.action_faq -> {
                toCustomTabs(if (inChina == true) "https://janyostudio.mystery0.vip/wa/faq.html" else "https://janyostudio.mystery0.app/wa/faq.html")
                true
            }
            R.id.action_why_ad -> {
                adsDialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}
