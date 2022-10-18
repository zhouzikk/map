package gdut.jsj.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import gdut.jsj.room.entity.User

@Dao
interface UserDao {
    @Insert
    fun insert(user: User)

    @Query("SELECT * FROM user_tb WHERE user=:user")
    fun query(user: String): User?
}