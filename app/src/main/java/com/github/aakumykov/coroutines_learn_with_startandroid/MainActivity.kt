package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import android.util.TimeUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val coroutineScope: CoroutineScope by lazy { CoroutineScope(Job()) }

    private var currentJob: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.launchButton1.setOnClickListener { onLaunch2ButtonClicked() }
        binding.cancelButton.setOnClickListener { onCancelButtonClicked() }
    }

    private fun onLaunch2ButtonClicked() {
        log("--------------- onLaunch2ButtonClicked() ---------------")
        log("перед coroutineScope.launch")
        currentJob = coroutineScope.launch {
            repeat(5) { i ->
                if (!isActive) {
                    log("Корутина стала неактивна")
                    return@repeat
                }
                else {
                    log("Ожидание $i ...")
                    TimeUnit.SECONDS.sleep(1)
                }
            }
            log("Ожидание завершено")
        }
        log("после coroutineScope.launch")
    }

    private fun onLaunch1ButtonClicked() {
        log("-------------------------------------------")
        log("Перед запуском suspend-фнукции")
        currentJob = coroutineScope.launch {
            try {
                download("файл://путь-к-файлу.txt")
            } catch (e: CancellationException) {
                log("Корутина отменена: ${e.message}")
            }
        }
        log("После запуска suspend-фнукции")
    }

    private fun onCancelButtonClicked() {
        log("onCancelButtonClicked()")
        currentJob?.also {
            it.cancel(CancellationException("Отменено пользователем"))
            currentJob = null
        } ?: run {
            log("Нечего отменять")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel(CancellationException("Scope отменён в onDestroy()"))
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