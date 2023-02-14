package dev.gaborbiro.notes.store.db

class TransactionProviderImpl(
    private val appDatabase: AppDatabase
) : TransactionProvider {

    override suspend fun runInTransaction(run: suspend () -> Unit) {
        appDatabase.beginTransaction()
        run()
        appDatabase.setTransactionSuccessful()
        appDatabase.endTransaction()
    }
}