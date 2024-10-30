package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding


    private var _scope: CoroutineScope? = null
    private val scope get() = _scope!!


    var job: Job? = null


    val handler = CoroutineExceptionHandler { context, throwable ->
        log("Обработчик исключений корутины: $throwable поймано.")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.launchButton1.setOnClickListener { runSample() }
        binding.cancelButton.setOnClickListener { cancelSample() }

    }


    private fun runSample() {
        prepareScope()

        job = scope.launch(CoroutineName("1")) {

            launch(CoroutineName("1_1")) {
                delay(1000)
            }

            launch(CoroutineName("1_2")) {
                delay(2000)
            }

            launch(CoroutineName("1_3")) {
                delay(3000)
            }

            launch(CoroutineName("1_4")) {
                delay(4000)
                logCorName()
            }

        }
    }

    private fun CoroutineScope.logCorName() {
        log("Корутина: ${coroutineContext[CoroutineName]?.name}")
    }


    private fun cancelSample() {
        job?.cancel(CancellationException("Отменено пользователем"))
    }


    private fun prepareScope() {
        _scope = CoroutineScope(Job() + Dispatchers.IO)
    }


    private fun log(text: String) {
        val thread = Thread.currentThread()
        val threadName = thread.name
        val threadHashCode = thread.hashCode()
        Log.d(TAG, text)
    }
}