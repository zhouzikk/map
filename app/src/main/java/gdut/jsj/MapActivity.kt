package gdut.jsj

import android.content.Context
import android.content.Intent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.util.JsonToken
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import gdut.jsj.databinding.ActivityMainBinding
import gdut.jsj.room.entity.MoveBean

class MapActivity : AppCompatActivity() {

    companion object {
        fun start(c: Context, data: MoveBean) {
            c.startActivity(Intent(c, MapActivity::class.java).apply {
                putExtra("data", data)
            })
        }
    }

    //起点图标
    var startBD = BitmapDescriptorFactory.fromResource(R.mipmap.ic_me_history_startpoint)

    //终点图标
    var finishBD = BitmapDescriptorFactory.fromResource(R.mipmap.ic_me_history_finishpoint)

    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.start.isVisible = false
        binding.list.isVisible = false

        val data = intent.getSerializableExtra("data") as MoveBean

        val moves =
            GsonUtils.fromJson<List<LatLng>>(data.moves, object : TypeToken<List<LatLng>>() {}.type)

        val oStart = MarkerOptions().apply {
            position(moves.first()) // 覆盖物位置点，第一个点为起点
            icon(startBD) // 设置覆盖物图片
        } // 地图标记覆盖物参数配置类
        binding.mapView.map.addOverlay(oStart) // 在地图上添加此图层

        //将points集合中的点绘制轨迹线条图层，显示在地图上
        val ooPolyline: OverlayOptions =
            PolylineOptions().width(13).color(-0x55010000).points(moves)
        binding.mapView.map.addOverlay(ooPolyline) as Polyline

        val oEnd = MarkerOptions().apply {
            position(moves.last()) // 覆盖物位置点，最后一个终点
            icon(finishBD) // 设置覆盖物图片
        } // 地图标记覆盖物参数配置类
        binding.mapView.map.addOverlay(oEnd)

        val locData = MyLocationData.Builder().accuracy(0f) // 此处设置开发者获取到的方向信息，顺时针0-360
            .latitude(moves.first().latitude)
            .longitude(moves.first().longitude).build()
        binding.mapView.map.setMyLocationData(locData)
        val builder = MapStatus.Builder()
        builder!!.target(moves.first()).zoom(18f)
        binding.mapView.map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder!!.build()))

    }

}