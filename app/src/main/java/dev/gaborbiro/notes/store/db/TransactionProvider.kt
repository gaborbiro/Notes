package dev.gaborbiro.notes.store.db

interface TransactionProvider {

    suspend fun runInTransaction(run: suspend () -> Unit)
}