//package pw.janyo.whatanime.activity
//
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.appcompat.widget.Toolbar
//import androidx.recyclerview.widget.ItemTouchHelper
//import android.view.View
//
//import org.litepal.crud.DataSupport
//
//import pw.janyo.whatanime.R
//import pw.janyo.whatanime.adapter.HistoryAdapter
//import pw.janyo.whatanime.classes.History
//import pw.janyo.whatanime.util.WAFileUtil
//
//class HistoryActivity : AppCompatActivity() {
//	private var toolbar: Toolbar? = null
//
//	override fun onCreate(savedInstanceState: Bundle?) {
//		super.onCreate(savedInstanceState)
//		initialization()
//		monitor()
//	}
//
//	private fun initialization() {
//		setContentView(R.layout.activity_history)
//		toolbar = findViewById(R.id.toolbar)
//
//		val list = WAFileUtil.checkList(DataSupport.findAll(History::class.java))
//		val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
//		recyclerView.layoutManager = LinearLayoutManager(this@HistoryActivity)
//		val adapter = HistoryAdapter(this@HistoryActivity, list)
//		val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT) {
//			override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
//				return false
//			}
//
//			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//				WAFileUtil.deleteHistory(list[viewHolder.adapterPosition])
//				list.removeAt(viewHolder.adapterPosition)
//				adapter.notifyItemRemoved(viewHolder.adapterPosition)
//			}
//		}
//		ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
//		recyclerView.adapter = adapter
//
//		setSupportActionBar(toolbar)
//	}
//
//	private fun monitor() {
//		toolbar!!.setNavigationOnClickListener { finish() }
//	}
//}
