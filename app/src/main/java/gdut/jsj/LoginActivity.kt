package gdut.jsj

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import gdut.jsj.databinding.ActivityLoginBinding
import gdut.jsj.room.MoveDataBase
import gdut.jsj.room.entity.User
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val account = binding.account.text.toString().trim()//获取账号
            val password = binding.password.text.toString().trim()//获取密码
            thread {
                MoveDataBase.instanse.userDao().query(account).let {
                    runOnUiThread {
                        if (it == null || it.password != password) {
                            ToastUtils.showShort("账号或者密码不正确")
                            return@runOnUiThread
                        }
                        USER_ID = it.id
                        ActivityUtils.startActivity(MainActivity::class.java)
                    }
                }
            }
        }

        binding.register.setOnClickListener {
            val account = binding.account.text.toString().trim()//获取账号
            val password = binding.password.text.toString().trim()//获取密码
            thread {
                MoveDataBase.instanse.userDao().insert(
                    User(
                        user = account,
                        password = password
                    )
                )
                runOnUiThread {
                    ToastUtils.showShort("注册成功")
                }
            }
        }

    }

}