package gdut.jsj

import android.app.Application
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        SDKInitializer.initialize(this)
        SDKInitializer.setCoordType(CoordType.BD09LL)
    }
}

var USER_ID = -1L