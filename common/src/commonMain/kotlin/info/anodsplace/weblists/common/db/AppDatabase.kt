package info.anodsplace.weblists.common.db

import info.anodsplace.weblists.common.extensions.toLong
import info.anodsplace.weblists.common.samples.MatchTv
import info.anodsplace.weblists.common.samples.Wikipedia
import info.anodsplace.weblists.db.WebListsDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppDatabase(private val db: WebListsDb) {
    val webSites = db.webListQueries

    private suspend fun insert(site: WebSite, lists: (siteId: Long) -> List<WebList>) = withContext(Dispatchers.Default){
        db.transaction {
            db.webSiteQueries.insert(
                site.url, site.title
            )
            val siteId = db.webSiteQueries.lastInsertId().executeAsOne()
            val entities = lists(siteId)
            entities.forEach {
                val transformation = it.apply.toString()
                db.webListQueries.insert(
                    it.siteId, it.order.toLong(), it.cssQuery, it.horizontal.toLong(), transformation
                )
            }
        }
    }

    suspend fun preload() = withContext(Dispatchers.Default) {
        val sites = loadSites()
        if (sites.isNotEmpty()) {
            db.webSiteQueries.deleteSites()
        }
        insert(
            WebSite(
                url = "https://matchtv.ru/tvguide",
                title = "match.tv"
            )
        ) { siteId -> MatchTv.sample(siteId) }
        insert(
            WebSite(
                url = "https://en.wikipedia.org/wiki/Main_Page",
                title = "Wikipedia"
            )
        ) { siteId -> Wikipedia.sample(siteId) }
    }

    suspend fun loadSites(): List<WebSite> = withContext(Dispatchers.Default) {
        return@withContext db.webSiteQueries.loadSites().executeAsList().map {
            WebSite(it)
        }
    }

    suspend fun loadSiteById(siteId: Long): WebSite = withContext(Dispatchers.Default) {
        val dbSite = db.webSiteQueries.loadById(siteId).executeAsOne()
        return@withContext WebSite(dbSite)
    }

    suspend fun loadSiteListsById(siteId: Long): WebSiteLists = withContext(Dispatchers.Default) {
        val dbSite = db.webSiteQueries.loadById(siteId).executeAsOne()
        val dbLists = db.webListQueries.loadBySiteId(siteId).executeAsList()

        return@withContext WebSiteLists(
            site = WebSite(dbSite),
            lists = dbLists.map { WebList(it) }
        )
    }

}