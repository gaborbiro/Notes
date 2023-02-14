package dev.gaborbiro.notes.store.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import dev.gaborbiro.notes.store.db.records.RecordsDAO
import dev.gaborbiro.notes.store.db.records.TemplatesDAO
import dev.gaborbiro.notes.store.db.records.model.RecordDBModel
import dev.gaborbiro.notes.store.db.records.model.TemplateDBModel

@Database(
    entities = [RecordDBModel::class, TemplateDBModel::class],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = AppDatabase.DeleteNoteMigration_2_3::class),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun recordsDAO(): RecordsDAO
    abstract fun templatesDAO(): TemplatesDAO

    companion object {

        @Volatile
        private lateinit var INSTANCE: AppDatabase

        fun init(appContext: Context) {
            INSTANCE = buildDatabase(appContext)
        }

        fun getInstance() = INSTANCE

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_db"
            ).build()
        }
    }

    @DeleteColumn(
        tableName = "records",
        columnName = "notes"
    )
    class DeleteNoteMigration_2_3 : AutoMigrationSpec
}