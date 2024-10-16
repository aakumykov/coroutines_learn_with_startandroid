package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMainBinding

    private val networkService: NetworkService by lazy { NetworkService(cacheDir) }

    private lateinit var scope: CoroutineScope

    private var currentJob: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.runFoldedCoroutinesWithoutJoin.setOnClickListener { runFoldedCoroutinesWithoutJoin() }
        binding.runFoldedCoroutinesWithJoin.setOnClickListener { runFoldedCoroutinesWithJoin() }
        binding.runParallelCoroutinesWithoutJoin.setOnClickListener { runParallelCoroutinesWithoutJoin() }
        binding.runParallelCoroutinesWithImmediateJoin.setOnClickListener { runParallelCoroutinesWithImmediateJoin() }
        binding.runParallelCoroutinesWithDeferredJoin.setOnClickListener { runParallelCoroutinesWithDeferredJoin() }
        binding.cancelButton.setOnClickListener { onCancelButtonClicked() }
    }

    private fun runFoldedCoroutinesWithoutJoin() {
        log("---------------- runFoldedCoroutinesWithoutJoin ---------------")
        scope = CoroutineScope(Job())
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
        scope = CoroutineScope(Job())

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
        scope = CoroutineScope(Job())

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
        scope = CoroutineScope(Job())

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
        scope = CoroutineScope(Job())

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
}