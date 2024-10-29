package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runSample()
    }

    private var _scope: CoroutineScope? = null
    private val scope get() = _scope!!

    val handler = CoroutineExceptionHandler { context, throwable ->
//        log("*** coroutineExceptionHandler ***")
        log("$throwable поймано в Coroutine_${context[CoroutineName]?.name}")
        Log.e(TAG, throwable.message, throwable)
    }

    private fun prepareScope() {
        _scope = CoroutineScope(Job() + Dispatchers.IO + handler)
    }


    fun CoroutineScope.repeatIsActive() {
        repeat(5) {
            TimeUnit.MILLISECONDS.sleep(300)
            log("Coroutine_${coroutineContext[CoroutineName]?.name} isActive $isActive")
        }
    }


    private fun runSample() {
        prepareScope()

        scope.launch(CoroutineName("1")) {
            launch(CoroutineName("1_1")) {
                TimeUnit.MILLISECONDS.sleep(1000)
                log("exception")
                Integer.parseInt("a")
            }
            launch(CoroutineName("1_2")) { repeatIsActive() }
            repeatIsActive()
        }

        scope.launch(CoroutineName("2")) {
            launch(CoroutineName("2_1")) { repeatIsActive() }
            launch(CoroutineName("2_2")) { repeatIsActive() }
            repeatIsActive()
        }
    }

    private fun log(text: String) {
        val thread = Thread.currentThread()
        val threadName = thread.name
        val threadHashCode = thread.hashCode()
        Log.d(TAG, text)
    }
}