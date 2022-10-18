package gdut.jsj.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "move_info_tb")
data class MoveBean(
    var userId: Long,
    var time: String,//开始时间
    var startLocation: String,//开始地点
    var endLocation: String,//结束地点
    var mileage: Double, //运动距离
    var moves: String //移动取点
) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}