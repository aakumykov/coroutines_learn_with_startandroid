package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
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

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        log("*** coroutineExceptionHandler ***")
        Log.e(TAG, throwable.message, throwable)
    }

    private fun prepareScope() {
        _scope = CoroutineScope(SupervisorJob() + handler)
    }

    private fun runSample() {
        prepareScope()

        scope.launch {
            repeat(5) { i ->
//                TimeUnit.MILLISECONDS.sleep(300)
                delay(300)
                log("1-я корутина ... ${i} (isActive:${isActive})")
            }
        }

        scope.launch {
//            TimeUnit.MILLISECONDS.sleep(1000)
            delay(1000)
            log("2-я корутина")
            Integer.parseInt("a")
        }
    }

    private fun log(text: String) {
        val thread = Thread.currentThread()
        val threadName = thread.name
        val threadHashCode = thread.hashCode()
        Log.d(TAG, text)
    }
}