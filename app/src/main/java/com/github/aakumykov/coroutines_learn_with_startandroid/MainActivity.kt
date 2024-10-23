package com.github.aakumykov.coroutines_learn_with_startandroid

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.aakumykov.coroutines_learn_with_startandroid.databinding.ActivityMainBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
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

    private var lazyJob: Job? = null
    private var asyncResult: Deferred<Int>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.twoLaunchesWithoutJoin.setOnClickListener { twoLaunchesWithoutJoin() }
        binding.twoLaunchesWithJoin.setOnClickListener { twoLaunchesWithJoin() }
//        binding.runParallelCoroutinesWithoutJoin.setOnClickListener { runParallelCoroutinesWithoutJoin() }
//        binding.runParallelCoroutinesWithImmediateJoin.setOnClickListener { runParallelCoroutinesWithImmediateJoin() }
//        binding.runParallelCoroutinesWithDeferredJoin.setOnClickListener { runParallelCoroutinesWithDeferredJoin() }
//        binding.createLazyCoroutine.setOnClickListener { createLazyCoroutine() }
//        binding.runLazyCoroutine.setOnClickListener { runLazyCoroutine() }
//        binding.createAsyncCoroutine.setOnClickListener { createAsyncCoroutine() }
//        binding.runAsyncCoroutine.setOnClickListener { runAsyncCoroutine() }
//        binding.parallelWork.setOnClickListener { parallelWork() }

        binding.cancelButton.setOnClickListener { onCancelButtonClicked() }
    }


    private fun twoLaunchesWithoutJoin() {
        logMethodName("twoLaunchesWithoutJoin")
        prepareScope()


        log("Перед запуском 1 launch")
        scope.launch {
            log("Внутри 1 launch, перед работой")
            TimeUnit.SECONDS.sleep(2)
            log("Внутри 1 launch, после работы")
        }
        log("После запуска 1 launch")


        log("Перед запуском 2 launch")
        scope.launch {
            log("Внутри 2 launch, перед работой")
            TimeUnit.SECONDS.sleep(3)
            log("Внутри 2 launch, после работы")
        }
        log("После запуска 2 launch")
    }
    /* Вывод: launch{} без .join() позволяет запускать параллельно независимые задачи */

    private fun CoroutineScope.jobIsActiveToLog(): String = ", job.isActive:${isActive}"

    fun CoroutineScope.logFromScope(text: String) {
        log("${text}, scope.isActive: ${isActive}")
    }


    private fun twoLaunchesWithJoin() {
        logMethodName("twoLaunchesWithJoin")
        prepareScope()

        scope.logFromScope("┎---------- scope.launch (старт) ----------┒")
        scope.launch {

            logFromScope("Перед запуском 1 launch")
            scope.launch {
                logFromScope("Внутри 1 launch, перед работой")
                TimeUnit.SECONDS.sleep(2)
                logFromScope("Внутри 1 launch, после работы")
            }.join()
            logFromScope("После запуска 1 launch")


            logFromScope("Перед запуском 2 launch")
            scope.launch {
                logFromScope("Внутри 2 launch, перед работой")
                TimeUnit.SECONDS.sleep(3)
                logFromScope("Внутри 2 launch, после работы")
            }.join()
            logFromScope("После запуска 2 launch")

        }
        scope.logFromScope("┖---------- scope.launch (финиш) ----------┚")
    }
    /* Запуск с .join() - последовательное выполнение внутри родительского scope.launch{},
    * тогда как этот родительский launch отрабатывает и завершается, не дожидаясь завершения
    * дочерних launch. По сути, дочерние параллельные launch выполняются последовательно в
    * фоновом потоке...
    *
    * Добавил вывод scope(job).isActive (какой это job?). Что-то принципиально не поменялось:
    * isActive всегда true.
    */


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
        Log.d(TAG, "[$threadName {$threadHashCode}] $text")
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

    private fun logMethodName(name: String) {
        log("-------------------- ${name}() ------------------")
    }

    private fun logParentStart(text: String) {
        log("┎---------- ${text}() ----------┒")
    }

    private fun logParentEnd(text: String) {
        log("┖---------- ${text}() ----------┚")
    }
}