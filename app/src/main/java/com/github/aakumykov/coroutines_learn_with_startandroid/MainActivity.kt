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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    private var result: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.launchButton1.setOnClickListener { runSample() }

        runSample()
    }

    private var _scope: CoroutineScope? = null
    private val scope get() = _scope!!

    val handler = CoroutineExceptionHandler { context, throwable ->
        log("Обработчик исключений корутины: $throwable поймано в Coroutine_${context[CoroutineName]?.name}, result: ${result}")
//        Log.e(TAG, throwable.message, throwable)
    }


    private fun prepareScope() {
        _scope = CoroutineScope(Job() + Dispatchers.IO + handler)
    }


    private fun runSample() {
        prepareScope()

        scope.launch {
            log("Случайное число: ${getRandomNum()}")
        }
    }

    private val randomBoolean get() = Random.nextBoolean()

    private suspend fun getRandomNum(): Int {
        return suspendCoroutine { continuation: Continuation<Int> ->

            val randomInt: Int = Random.nextInt(31)

            if (randomInt <= 10) {
                if (randomBoolean)
                    continuation.resume(randomInt)
                else
                    continuation.resumeWithException(Exception("Исключение в синхронной части корутины (i=$randomInt)."))
            }
            else {
                thread {
                    if (randomInt <= 20) {
                        continuation.resume(randomInt)
                    }
                    else {
                        continuation.resumeWithException(Exception("Исключение в асинхронной части корутины (i=$randomInt)."))
                    }
                }
            }
        }
    }

    private fun log(text: String) {
        val thread = Thread.currentThread()
        val threadName = thread.name
        val threadHashCode = thread.hashCode()
        Log.d(TAG, text)
    }

    private fun CoroutineScope.repeatIsActive() {
        repeat(5) {
            TimeUnit.MILLISECONDS.sleep(300)
            log("Coroutine_${coroutineContext[CoroutineName]?.name} isActive $isActive")
        }
    }
}