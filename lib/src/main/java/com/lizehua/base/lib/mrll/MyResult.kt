package com.lizehua.base.lib.mrll

/**
 *  资源加载的结构封装类
 */
class MyResult<T> {

    var value: T? = null
        private set

    var exception: Throwable? = null
        private set

    constructor(value: T) {
        this.value = value
        exception = null
    }

    constructor(e: Throwable) {
        exception = e
        value = null
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is MyResult<*>) {
            return false
        }
        val that = o as MyResult<*>?
        if (value != null && value == that!!.value) {
            return true
        }
        return if (exception != null && that!!.exception != null) {
            exception!!.toString() == exception!!.toString()
        } else false
    }

    override fun hashCode(): Int {
        return arrayOf(value, exception).contentHashCode()
    }

}