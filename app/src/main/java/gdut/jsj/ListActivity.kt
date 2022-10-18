package gdut.jsj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import gdut.jsj.databinding.ActivityListBinding
import gdut.jsj.room.MoveDataBase
import gdut.jsj.room.entity.MoveBean
import kotlin.concurrent.thread

class ListActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityListBinding.inflate(layoutInflater)
    }

    val adapter by lazy {
        object : BaseQuickAdapter<MoveBean, BaseViewHolder>(R.layout.item_move) {
            override fun convert(holder: BaseViewHolder, item: MoveBean) {
                holder.setText(R.id.time, "时间：${item.time}")
                    .setText(R.id.start, "开始地点：${item.startLocation}")
                    .setText(R.id.end, "结束地点：${item.endLocation}")
                    .setText(R.id.mileage, "距离：${item.mileage}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recyclerView.adapter = adapter

        thread {
            MoveDataBase.instanse.moveDao().quert(USER_ID)?.let {
                runOnUiThread {
                    adapter.setNewInstance(it as MutableList<MoveBean>)
                }
            }
        }

        adapter.setOnItemClickListener { _, _, a ->
            MapActivity.start(this, adapter.getItem(a))
        }

    }
}