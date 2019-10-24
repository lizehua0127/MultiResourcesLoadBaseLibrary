package com.lizehua.base.lib.mrll

import androidx.collection.LruCache


/**
 * lru 的资源缓存
 */
object MyCompositionCache {

    private val cache = LruCache<String, MyComposition>(20)

    fun get(cacheKey: String?): MyComposition? {
        return if (cacheKey == null) {
            null
        } else cache.get(cacheKey)
    }

    fun put(cacheKey: String?, composition: MyComposition) {
        if (cacheKey == null) {
            return
        }
        cache.put(cacheKey, composition)
    }

    fun clear() {
        cache.evictAll()
    }

    /**
     * Set the maximum number of compositions to keep cached in memory.
     * This must be > 0.
     */
    fun resize(size: Int) {
        cache.resize(size)
    }

}