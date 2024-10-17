package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    private val networkService: NetworkService by lazy { NetworkService(cacheDir) }

    private lateinit var scope: CoroutineScope

    private var lazyJob: Job? = null
    private var asyncResult: Deferred<Int>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.runFoldedCoroutinesWithoutJoin.setOnClickListener { runFoldedCoroutinesWithoutJoin() }
        binding.runFoldedCoroutinesWithJoin.setOnClickListener { runFoldedCoroutinesWithJoin() }
        binding.runParallelCoroutinesWithoutJoin.setOnClickListener { runParallelCoroutinesWithoutJoin() }
        binding.runParallelCoroutinesWithImmediateJoin.setOnClickListener { runParallelCoroutinesWithImmediateJoin() }
        binding.runParallelCoroutinesWithDeferredJoin.setOnClickListener { runParallelCoroutinesWithDeferredJoin() }
        binding.createLazyCoroutine.setOnClickListener { createLazyCoroutine() }
        binding.runLazyCoroutine.setOnClickListener { runLazyCoroutine() }
        binding.createAsyncCoroutine.setOnClickListener { createAsyncCoroutine() }
        binding.runAsyncCoroutine.setOnClickListener { runAsyncCoroutine() }

        binding.cancelButton.setOnClickListener { onCancelButtonClicked() }
    }

    
    private fun createAsyncCoroutine() {
        log("----------------- createAsyncCoroutine() ----------------")
        prepareScope()

        log("Перед созданием deferred-корутины")
        asyncResult = scope.async(start = CoroutineStart.LAZY) {
            log("Async-корутина, запуск")
            TimeUnit.SECONDS.sleep(2)
            val res = Random.nextInt(100)
            log("Async-корутина, завершение и возврат значения")
            return@async res
        }
        log("После созданием deferred-корутины")
    }

    private fun runAsyncCoroutine() {
        log("------------- runAsyncCoroutine() -------------")
        asyncResult?.also { deferred ->

            log("Перед запуском scope.launch для получения результата из deferred")
            scope.launch {
                log("Перед получением результата из deferred")
                log("Результат: ${deferred.await()}")
                log("После получением результата из deferred")
            }
            log("После запуском scope.launch для получения результата из deferred")

        } ?: run {
            showError("Async-корутина не подготовлена")
        }
    }


    private fun createLazyCoroutine() {
        prepareScope()
        log("-------------- createLazyCoroutine() ---------------")
        lazyJob = scope.launch(start = CoroutineStart.LAZY) {
            log("Ленивая корутина, запуск")
            TimeUnit.SECONDS.sleep(1)
            log("Ленивая корутина, завершение")
        }
    }

    private fun runLazyCoroutine() {
        log("-------------- runLazyCoroutine() ---------------")
        lazyJob?.start() ?: run {
            showError("Корутина не подготовлена")
        }
    }

    private fun runFoldedCoroutinesWithoutJoin() {
        log("---------------- runFoldedCoroutinesWithoutJoin ---------------")
        prepareScope()
        scope.launch {
            log("Родительская корутина, запуск")
            launch {
                log("Дочерняя корутина, запуск")
                TimeUnit.SECONDS.sleep(1)
                log("Дочерняя корутина, завершение")
            }
            log("Родительская корутина, завершение")
        }
    }

    private fun runFoldedCoroutinesWithJoin() {
        log("---------- runFoldedCoroutinesWithJoin ----------")
        prepareScope()

        log("before outer launch")
        scope.launch {
            log("Родительская корутина, запуск")
            scope.launch {
                var i=1
                log("ожидание начато ...")
                while (i <= 5 && isActive) {
                    log("жду $i ...")
                    TimeUnit.SECONDS.sleep(1)
                    i++
                }
                log("... ожидание завершено")
            }.apply {
                join()
            }
            log("Родительская корутина, завершение")
        }
        log("after outer launch")
    }

    private fun runParallelCoroutinesWithoutJoin() {
        log("---------- runParallelCoroutinesWithoutJoin ---------")
        prepareScope()

        scope.launch {
            log("Родительская корутина, запуск")

            scope.launch {
                log("Корутина-1, запуск")
                TimeUnit.SECONDS.sleep(2)
                log("Корутина-1, завершение")
            }

            scope.launch {
                log("Корутина-2, запуск")
                TimeUnit.SECONDS.sleep(1)
                log("Корутина-2, завершение")
            }

            log("Родительская корутина, завершение")
        }
    }

    private fun runParallelCoroutinesWithImmediateJoin() {
        log("---------- runParallelCoroutinesWithJoin ---------")
        prepareScope()

        scope.launch {
            log("Родительская корутина, запуск")

            scope.launch {
                log("Корутина-1, запуск")
                TimeUnit.SECONDS.sleep(2)
                log("Корутина-1, завершение")
            }.join()

            scope.launch {
                log("Корутина-2, запуск")
                TimeUnit.SECONDS.sleep(1)
                log("Корутина-2, завершение")
            }.join()

            log("Родительская корутина, завершение")
        }
    }

    private fun runParallelCoroutinesWithDeferredJoin() {
        log("---------- runParallelCoroutinesWithDeferredJoin ---------")
        prepareScope()

        scope.launch {
            log("Родительская корутина, запуск")

            val job1 = scope.launch {
                log("Корутина-1, запуск")
                TimeUnit.SECONDS.sleep(2)
                log("Корутина-1, завершение")
            }

            val job2 = scope.launch {
                log("Корутина-2, запуск")
                TimeUnit.SECONDS.sleep(1)
                log("Корутина-2, завершение")
            }

            job1.join()
            job2.join()

            log("Родительская корутина, завершение")
        }
    }




    private fun onCancelButtonClicked() {
        log("onCancelButtonClicked()")
        /*currentJob?.apply {
            cancel(CancellationException("Отменено пользователем"))
            currentJob = null
        } ?: run {
            log("Текущая задача не найдена")
        }*/
        scope.cancel(CancellationException("Scope отменён пользователем"))
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel(CancellationException("Scope отменён в onDestroy()"))
    }

    private fun log(text: String) {
        val thread = Thread.currentThread()
        val threadName = thread.name
        val threadHashCode = thread.hashCode()
        Log.d(TAG, "[$threadName{$threadHashCode}] $text")
    }

    private suspend fun download(url: String): File {
        log("suspend fun download(), перед запуском suspendCoroutine{}")
        return suspendCoroutine { continuation ->
            log("suspend fun download(), внутри suspendCoroutine, перед networkService.download()")
            networkService.download(url, object: NetworkService.Callback {
                override fun onSuccess(downloadedFile: File) {
                    log("suspend fun download(), внутри suspendCoroutine, внутри networkService.Callback()")
                    continuation.resume(downloadedFile)
                }
            })
        }
    }

    class NetworkService(private val cacheDir: File) {

        fun download(url: String, callback: Callback) {
            thread {
                TimeUnit.SECONDS.sleep(3)
                callback.onSuccess(File(cacheDir,"some_file.txt"))
            }
        }

        interface Callback {
            fun onSuccess(downloadedFile: File)
//            fun onError(e: Exception)
        }
    }

    private fun prepareScope() {
        scope = CoroutineScope(Job())
    }

    private fun showToast(text: String) {
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
    }

    private fun showError(errorMsg: String) {
        log(errorMsg)
        showToast(errorMsg)
    }
}