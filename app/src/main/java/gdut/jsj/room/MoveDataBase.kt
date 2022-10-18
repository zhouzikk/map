package gdut.jsj.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.blankj.utilcode.util.Utils
import gdut.jsj.room.dao.MoveDao
import gdut.jsj.room.dao.UserDao
import gdut.jsj.room.entity.MoveBean
import gdut.jsj.room.entity.User

@Database(entities = [User::class, MoveBean::class], version = 1)
abstract class MoveDataBase : RoomDatabase() {

    companion object {
        val instanse: MoveDataBase by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Room.databaseBuilder(
                Utils.getApp(),
                MoveDataBase::class.java,
                "move_db"
            ).build()
        }
    }

    abstract fun userDao(): UserDao
    abstract fun moveDao(): MoveDao

}