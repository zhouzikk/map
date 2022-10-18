package gdut.jsj.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import gdut.jsj.room.entity.MoveBean

@Dao
interface MoveDao {
    @Insert
    fun insert(bean: MoveBean)

    @Query("SELECT * FROM move_info_tb WHERE userId=:id")
    fun quert(id: Long): List<MoveBean>?
}