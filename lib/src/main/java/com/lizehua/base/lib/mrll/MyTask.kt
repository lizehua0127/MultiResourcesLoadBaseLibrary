package com.lizehua.base.lib.mrll

import android.os.Handler
import android.os.Looper
import java.util.*
import java.util.concurrent.*
import kotlin.collections.ArrayList

/**
 * 异步任务执行类
 */
class MyTask<T> {

    companion object{
        private val EXECUTOR: Executor = Executors.newCachedThreadPool()
    }
    private val successListeners = LinkedHashSet<MyListener<T>>(1)
    private val failureListeners = LinkedHashSet<MyListener<Throwable>>(1)
    private val handler = Handler(Looper.getMainLooper())

    @Volatile
    private var result: MyResult<T>? = null


    constructor(runnable: Callable<MyResult<T>>) : this(runnable, false)

    constructor(runnable: Callable<MyResult<T>>, runNow: Boolean) {
        if (runNow) {
            try {
                setResult(runnable.call())
            } catch (e: Throwable) {
                setResult(MyResult(e))
            }

        } else {
            EXECUTOR.execute(MyFutureTask(runnable))
        }
    }

    private fun setResult(result: MyResult<T>?) {
        check(this.result == null) { "A task may only be set once." }
        this.result = result
        notifyListeners()
    }

    private fun notifyListeners() {
        // Listeners should be called on the main thread.
        handler.post(Runnable {
            if (result == null) {
                return@Runnable
            }
            // Local reference in case it gets set on a background thread.
            val result = this@MyTask.result
            if (result!!.value != null) {
                notifySuccessListeners(result.value!!)
            } else {
                notifyFailureListeners(result.exception!!)
            }
        })
    }

    @Synchronized
    private fun notifySuccessListeners(value: T) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        val listenersCopy = ArrayList<MyListener<T>>(successListeners)
        for (l in listenersCopy) {
            l.onResult(value)
        }
    }

    @Synchronized
    private fun notifyFailureListeners(e: Throwable) {
        // Allows listeners to remove themselves in onResult.
        // Otherwise we risk ConcurrentModificationException.
        val listenersCopy = ArrayList<MyListener<Throwable>>(failureListeners)
        if (listenersCopy.isEmpty()) {
//            Log.w(L.TAG, "My encountered an error but no failure listener was added.", e)
            return
        }

        for (l in listenersCopy) {
            l.onResult(e)
        }
    }

    @Synchronized
    fun addListener(listener: MyListener<T>): MyTask<T> {
        if (result != null && result!!.value != null) {
            listener.onResult(result!!.value!!)
        }

        successListeners.add(listener)
        return this
    }

    /**
     * Remove a given task listener. The task will continue to execute so you can re-add
     * a listener if neccesary.
     * @return the task for call chaining.
     */
    @Synchronized
    fun removeListener(listener: MyListener<T>): MyTask<T> {
        successListeners.remove(listener)
        return this
    }


    @Synchronized
    fun addFailureListener(listener: MyListener<Throwable>): MyTask<T> {
        if (result != null && result!!.exception != null) {
            listener.onResult(result!!.exception!!)
        }

        failureListeners.add(listener)
        return this
    }

    @Synchronized
    fun removeFailureListener(listener: MyListener<Throwable>): MyTask<T> {
        failureListeners.remove(listener)
        return this
    }


    private inner class MyFutureTask internal constructor(callable: Callable<MyResult<T>>) :
        FutureTask<MyResult<T>>(callable) {

        override fun done() {
            if (isCancelled) {
                // We don't need to notify and listeners if the task is cancelled.
                return
            }

            try {
                setResult(get())
            } catch (e: InterruptedException) {
                setResult(MyResult<T>(e))
            } catch (e: ExecutionException) {
                setResult(MyResult<T>(e))
            }

        }
    }

}