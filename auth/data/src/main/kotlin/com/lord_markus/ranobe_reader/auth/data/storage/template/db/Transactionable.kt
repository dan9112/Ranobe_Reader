package com.lord_markus.ranobe_reader.auth.data.storage.template.db

import java.util.concurrent.Callable

interface Transactionable {
    fun <V : Any?> runInTransaction(body: Callable<V>): V
    fun runInTransaction(body: Runnable)
}
