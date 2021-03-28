package info.anodsplace.weblists

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import info.anodsplace.weblists.rule.TransformationContainer
import info.anodsplace.weblists.rule.WebList
import info.anodsplace.weblists.rule.WebSite
import info.anodsplace.weblists.rule.WebSiteStore

@Database(entities = [WebSite::class, WebList::class], version = 1)
@TypeConverters(TransformationContainer.Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun webSites(): WebSiteStore
}