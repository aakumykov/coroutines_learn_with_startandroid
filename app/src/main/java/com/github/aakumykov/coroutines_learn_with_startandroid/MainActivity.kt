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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.launchButton1.setOnClickListener { runSample() }
        binding.cancelButton.setOnClickListener { cancelSample() }

        binding.launchCancelableSuspendFunction.setOnClickListener { launchCancelableSuspendFunction() }
        binding.launchNonCancelableSuspendFunction.setOnClickListener { launchNonCancelableSuspendFunction() }
        binding.cancelSuspendButton.setOnClickListener { cancelSuspendButton() }

//        runSample()
    }


    private var suspendFunctionJob: Job? = null


    private fun launchNonCancelableSuspendFunction() {
        prepareScope()
        suspendFunctionJob = scope.launch {
            val res = nonCancelableSuspendFunction()
            log("Результат неотменяемой suspend-функции: $res")
        }
    }

    private suspend fun nonCancelableSuspendFunction(): String {
        return suspendCoroutine<String> { continuation ->
            var i=0
            while(i<=100) {
                log("nonCancelableSuspendFunction...$i")
                i+=10
                TimeUnit.MILLISECONDS.sleep(200)
            }
            continuation.resume("РНSФ")
        }
    }

    private fun launchCancelableSuspendFunction() {
        prepareScope()
        suspendFunctionJob = scope.launch {
            val res = cancelableSuspendFunction()
            log("Результат отменяемой suspend-функции: $res")
        }
    }

    private fun cancelSuspendButton() {
        suspendFunctionJob?.cancel(CancellationException("Желание пользователя"))
            ?: run { showToast("suspend-функции не запущены") }
    }


    private fun showToast(text: String) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
    }


    private suspend fun cancelableSuspendFunction(): String {
        return suspendCancellableCoroutine<String> { cancellableContinuation ->

            var isCancelled: Boolean = false

            cancellableContinuation.invokeOnCancellation { t: Throwable? ->
                log("Отменяемая suspend-функция ОТМЕНЕНА.")
                isCancelled = true
            }

            var i=1
            while(i<20 && !isCancelled) {
                log("cancelableSuspendFunction...$i")
                TimeUnit.MILLISECONDS.sleep(500)
                i++
            }
            cancellableContinuation.resume("РОSФ")
        }
    }


    private var _scope: CoroutineScope? = null
    private val scope get() = _scope!!

    val handler = CoroutineExceptionHandler { context, throwable ->
        log("Обработчик исключений корутины: $throwable поймано.")
//        Log.e(TAG, throwable.message, throwable)
    }


    private fun prepareScope() {
        _scope = CoroutineScope(Job() + Dispatchers.IO)
    }

    var job: Job? = null

    private fun runSample() {
        prepareScope()

        scope.launch (Dispatchers.IO) {

            launch (Dispatchers.IO) {
                launch (Dispatchers.IO) {
                    var i=1;
                    while(isActive) {
                        log("Родительская корутина: ${i++}")
                        TimeUnit.MILLISECONDS.sleep(500)
                    }
                }

                launch (Dispatchers.IO) {
                    launch (Dispatchers.IO) {
                        var j=1;
                        while(isActive) {
                            log("1-я дочерняя: ${j++}")
                            TimeUnit.MILLISECONDS.sleep(400)
                        }
                    }

                    job = launch (Dispatchers.IO) {
                        launch (Dispatchers.IO) {
                            var n=1;
                            while(isActive) {
                                log("2-я дочерняя: ${n++}")
                                TimeUnit.MILLISECONDS.sleep(300)
                            }
                        }

                        launch (Dispatchers.IO) {
                            var m=1;
                            while(isActive) {
                                log("3-я дочерняя: ${m++}")
                                TimeUnit.MILLISECONDS.sleep(200)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun cancelSample() {
        job?.cancel(CancellationException("Отменено пользователем"))
    }


    private val randomBoolean get() = Random.nextBoolean()

    private suspend fun getRandomNum(): Int {
        return suspendCoroutine { continuation: Continuation<Int> ->

            val randomInt: Int = Random.nextInt(31)

            if (randomInt <= 10) {
                if (randomBoolean)
                    continuation.resume(randomInt)
                else
                    Exception("Исключение в синхронной части suspend-функции (num=$randomInt)").also {
//                        continuation.resumeWithException(it)
                        throw it
                    }
            }
            else {
                thread {
                    if (randomInt <= 20) {
                        continuation.resume(randomInt)
                    }
                    else {
                        Exception("Исключение в асинхронной части suspend-функции (num=$randomInt)").also {
//                            continuation.resumeWithException(it)
                            throw it
                        }
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