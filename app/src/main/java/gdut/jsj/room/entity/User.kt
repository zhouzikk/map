package gdut.jsj.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_tb")
data class User(
    var user: String,
    var password: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}