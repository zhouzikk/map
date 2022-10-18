package gdut.jsj

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.*
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.utils.DistanceUtil
import com.blankj.utilcode.util.*
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import gdut.jsj.databinding.ActivityMainBinding
import gdut.jsj.room.MoveDataBase
import gdut.jsj.room.entity.MoveBean
import kotlin.concurrent.thread
import kotlin.math.abs

class MainActivity : AppCompatActivity(), SensorEventListener {

    //是否开始运动
    private var isStart = false

    //获取xmldatabinding
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    //定位相关对象
    private val mLocationClient by lazy {
        LocationClient(this)
    }

    //当前经纬度
    var mCurrentLat = -1.0
    var mCurrentLon = -1.0

    var mCurrentDirection = -1

    var locData: MyLocationData? = null

    var mCurrentZoom = 18f //默认地图缩放比例值


    var builder: MapStatus.Builder? = null


    var last = LatLng(0.0, 0.0) //上一个定位点

    var isFirstLoc = true //首次定位

    val points = mutableListOf<LatLng>()//定位集合

    var mPolyline: Polyline? = null//运动轨迹图层

    //起点图标
    var startBD = BitmapDescriptorFactory.fromResource(R.mipmap.ic_me_history_startpoint)

    //终点图标
    var finishBD = BitmapDescriptorFactory.fromResource(R.mipmap.ic_me_history_finishpoint)

    //起点定位
    var startLocation = ""

    //终点定位
    var endLocation = ""

    //移动距离
    var mileage = 0.0

    //开始时间
    var time = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //获取定位权限
        XXPermissions.with(this)
            .permission(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION)
            .request { _, all ->
                if (all)
                    ToastUtils.showShort("获取到权限")
                else
                    ToastUtils.showShort("未获取到定位权限，无法正常使用")
            }

        binding.mapView.map.apply {
            //开启地图的定位图层
            isMyLocationEnabled = true
            //设置为跟随模式
            setMyLocationConfiguration(
                MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.FOLLOWING,
                    true,
                    null
                )
            )

        }

        //初始化定位
        mLocationClient.locOption = LocationClientOption().apply {
            //精度
            locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            //坐标系
            setCoorType("bd09ll")
            //是否需要地址
            setIsNeedAddress(true)
            //开启GPS
            isOpenGps = true
            setScanSpan(1000)
        }

        mLocationClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(p0: BDLocation?) {
                p0?.let { bdl ->

                    //未开始运动 仅定位
                    if (!isStart) {
                        val ll = LatLng(bdl.latitude, bdl.longitude)
                        locateAndZoom(bdl, ll)
                        return
                    }

                    //首次定位 获取精度较高的起点
                    if (isFirstLoc) {
                        getMostAccuracyLocation(bdl)?.let { ll ->
                            isFirstLoc = false
                            points.add(ll)
                            last = ll
                            locateAndZoom(bdl, ll)

                            //标记起点图层位置
                            val oStart = MarkerOptions().apply {
                                position(points[0]) // 覆盖物位置点，第一个点为起点
                                icon(startBD) // 设置覆盖物图片
                            } // 地图标记覆盖物参数配置类
                            binding.mapView.map.addOverlay(oStart) // 在地图上添加此图层

                            startLocation = bdl.addrStr

                        }
                        return
                    }
                    //第二个点之后
                    val ll = LatLng(bdl.latitude, bdl.longitude)
                    //sdk回调gps位置的频率是1秒1个，位置点太近动态画在图上不是很明显，可以设置点之间距离大于为5米才添加到集合中
                    if (DistanceUtil.getDistance(last, ll) < 5) {
                        return
                    }
                    //计算距离
                    mileage += DistanceUtil.getDistance(last, ll)
                    points.add(ll)
                    last = ll
                    locateAndZoom(bdl, ll)

                    //清除上一次轨迹，避免重叠绘画
                    binding.mapView.map.clear()

                    //起始点图层也会被清除，重新绘画
                    val oStart = MarkerOptions().apply {
                        position(points[0])
                        icon(startBD)
                    }
                    binding.mapView.map.addOverlay(oStart)

                    //将points集合中的点绘制轨迹线条图层，显示在地图上
                    val ooPolyline: OverlayOptions =
                        PolylineOptions().width(13).color(-0x55010000).points(points)
                    mPolyline = binding.mapView.map.addOverlay(ooPolyline) as Polyline

                    endLocation = bdl.addrStr

                }
            }
        })

        mLocationClient.start()

        //点击事件
        binding.start.setOnClickListener {
            isStart = !isStart
            if (isStart) {
                binding.start.text = "结束运动"
                binding.mapView.map.clear()

                time = TimeUtils.getNowString()

            } else {
                binding.start.text = "开始运动"
                binding.mapView.map.clear()
                isFirstLoc=true
                //结束运动保存相关信息
                MoveBean(
                    time = time,
                    userId = USER_ID,
                    startLocation = startLocation,
                    endLocation = endLocation,
                    mileage = mileage,
                    moves = GsonUtils.toJson(points)
                ).let {
                    thread {
                        MoveDataBase.instanse.moveDao().insert(it)
                        ToastUtils.showShort("保存成功")
                    }
                }
                points.clear()
            }
        }

        binding.list.setOnClickListener {
            ActivityUtils.startActivity(ListActivity::class.java)
        }

    }

    var lastX = 0.0

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val x = sensorEvent.values[SensorManager.DATA_X].toDouble()
        if (abs(x - lastX) > 1.0) {
            mCurrentDirection = x.toInt()
            if (isFirstLoc) {
                lastX = x
                return
            }
            locData = MyLocationData.Builder().accuracy(0f) // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(mCurrentDirection.toFloat()).latitude(mCurrentLat).longitude(mCurrentLon)
                .build()
            binding.mapView.map.setMyLocationData(locData)
        }
        lastX = x
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
//        TODO("Not yet implemented")
    }

    private fun locateAndZoom(location: BDLocation, ll: LatLng) {
        mCurrentLat = location.latitude
        mCurrentLon = location.longitude
        locData = MyLocationData.Builder().accuracy(0f) // 此处设置开发者获取到的方向信息，顺时针0-360
            .direction(mCurrentDirection.toFloat()).latitude(location.latitude)
            .longitude(location.longitude).build()
        binding.mapView.map.setMyLocationData(locData)
        builder = MapStatus.Builder()
        builder!!.target(ll).zoom(mCurrentZoom)
        binding.mapView.map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder!!.build()))
    }

    /**
     * 首次定位很重要，选一个精度相对较高的起始点
     * 注意：如果一直显示gps信号弱，说明过滤的标准过高了，
     * 你可以将location.getRadius()>25中的过滤半径调大，比如>40，
     * 并且将连续5个点之间的距离DistanceUtil.getDistance(last, ll ) > 5也调大一点，比如>10，
     * 这里不是固定死的，你可以根据你的需求调整，如果你的轨迹刚开始效果不是很好，你可以将半径调小，两点之间距离也调小，
     * gps的精度半径一般是10-50米
     */
    private fun getMostAccuracyLocation(location: BDLocation): LatLng? {
//        if (location.radius > 40) { //gps位置精度大于40米的点直接弃用
//            return null
//        }
        val ll = LatLng(location.latitude, location.longitude)
//        if (DistanceUtil.getDistance(last, ll) > 10) {
//            last = ll
//            points.clear() //有任意连续两点位置大于10，重新取点
//            return null
//        }
//        points.add(ll)
//        last = ll
//        有5个连续的点之间的距离小于10，认为gps已稳定，以最新的点为起始点
//        if (points.size >= 5) {
//            points.clear()
        return ll
//        }
//        return null
    }

    override fun onResume() {
        super.onResume()
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        binding.mapView.onDestroy()
    }
}