package com.lizehua.base.lib.mrll

import android.content.Context
import android.util.JsonReader
import androidx.core.graphics.TypefaceCompatUtil.closeQuietly
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.Callable

/**
 * 资源加载的工厂类，可以从此处进行资源加载逻辑，
 * Callable进行资源加载逻辑编写，框架可以进行缓存与结果返回
 *
 */
object ResourcesFactory {

    private val taskCache = HashMap<String?, MyTask<MyComposition>>()

    fun fromAsset(context: Context, fileName: String): MyTask<MyComposition> {
        // Prevent accidentally leaking an Activity.
        val appContext = context.applicationContext
        return cache(fileName,
            Callable { fromAssetSync(appContext, fileName) })
    }

    private fun fromAssetSync(context: Context, fileName: String): MyResult<MyComposition> {
        return try {
            val cacheKey = "asset_$fileName"
            fromJsonInputStreamSync(context.assets.open(fileName), cacheKey)
        } catch (e: IOException) {
            MyResult(e)
        }
    }

    private fun fromJsonInputStreamSync(
        stream: InputStream,
        cacheKey: String?
    ): MyResult<MyComposition> {
        return fromJsonInputStreamSync(stream, cacheKey, true)
    }

    private fun fromJsonInputStreamSync(
        stream: InputStream,
        cacheKey: String?,
        close: Boolean
    ): MyResult<MyComposition> {
        try {
            return fromJsonReaderSync(JsonReader(InputStreamReader(stream)), cacheKey)
        } finally {
            if (close) {
                closeQuietly(stream)
            }
        }
    }


    private fun fromJsonReaderSync(reader: JsonReader, cacheKey: String?): MyResult<MyComposition> {
        return fromJsonReaderSyncInternal(reader, cacheKey, true)
    }

    private fun fromJsonReaderSyncInternal(
        reader: JsonReader, cacheKey: String?, close: Boolean
    ): MyResult<MyComposition> {
        return try {
            // todo 此处真正的耗时去加载资源，加载好后进行缓存
            val composition = MyComposition()
            composition.data = cacheKey
            composition.string = cacheKey

            MyCompositionCache.put(cacheKey, composition)
            MyResult(composition)
        } catch (e: Exception) {
            MyResult(e)
        } finally {
            if (close) {
                closeQuietly(reader)
            }
        }
    }

    private fun cache(
        cacheKey: String?, callable: Callable<MyResult<MyComposition>>
    ): MyTask<MyComposition> {
        val cachedComposition =
            if (cacheKey == null) null else MyCompositionCache.get(cacheKey)
        if (cachedComposition != null) {
            return MyTask(Callable { MyResult(cachedComposition) })
        }
        if (cacheKey != null && taskCache.containsKey(cacheKey)) {
            return taskCache[cacheKey]!!
        }

        val task = MyTask(callable)
        task.addListener(object : MyListener<MyComposition> {
            override fun onResult(result: MyComposition) {
                if (cacheKey != null) {
                    MyCompositionCache.put(cacheKey, result)
                }
                taskCache.remove(cacheKey)
            }
        })
        task.addFailureListener(object : MyListener<Throwable> {
            override fun onResult(result: Throwable) {
                taskCache.remove(cacheKey)
            }
        })
        taskCache[cacheKey] = task
        return task
    }


}