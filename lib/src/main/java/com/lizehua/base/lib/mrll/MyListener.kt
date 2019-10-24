package com.lizehua.base.lib.mrll

/**
 * 资源加载结果回调接口
 */
interface MyListener<T> {
    fun onResult(result: T)
}