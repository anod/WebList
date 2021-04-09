package info.anodsplace.weblists

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import info.anodsplace.weblists.rules.TransformationContainer
import info.anodsplace.weblists.rules.WebList
import info.anodsplace.weblists.rules.WebSite
import info.anodsplace.weblists.rules.WebSiteStore

@Database(entities = [WebSite::class, WebList::class], version = 1)
@TypeConverters(TransformationContainer.Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webSites(): WebSiteStore

    companion object {
        fun create(applicationContext: Context): AppDatabase {
            return Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "web-lists.db"
            )
                //.createFromAsset("database/myapp.db")
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}