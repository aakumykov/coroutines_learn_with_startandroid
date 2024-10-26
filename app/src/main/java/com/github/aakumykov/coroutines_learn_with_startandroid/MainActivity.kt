package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    private var _scope: CoroutineScope? = null
    private val scope: CoroutineScope get() = _scope!!

    private fun prepareScope() {
        _scope = CoroutineScope(Job())
    }

    override fun onDestroy() {
        super.onDestroy()
        _scope?.cancel(CancellationException("onDestroy()"))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.launchButton1.setOnClickListener { onLaunch1ButtonClicked() }
    }

    private fun onLaunch1ButtonClicked() {
        prepareScope()

        val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            log("*** coroutineExceptionHandler ***")
            Log.e(TAG, throwable.message, throwable)
        }

        scope.launch (coroutineExceptionHandler) {
            log("Родительская корутина")

            launch {
                repeat(5) { i ->
                    log("дочерняя-корутина-1 ... ${i+1}")
                    delay(500)
                }
            }

            launch {
                delay(Random.nextLong(100, 1501))
                log("дочерняя-корутина-2")
                Integer.parseInt("a")
            }
        }
    }

    private fun log(text: String) {
        val thread = Thread.currentThread()
        val threadName = thread.name
        val threadHashCode = thread.hashCode()
        Log.d(TAG, "[$threadName {$threadHashCode}] $text")
    }
}