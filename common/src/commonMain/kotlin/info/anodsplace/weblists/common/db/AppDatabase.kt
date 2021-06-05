package info.anodsplace.weblists.common.db

import info.anodsplace.weblists.common.extensions.toLong
import info.anodsplace.weblists.common.samples.MatchTv
import info.anodsplace.weblists.common.samples.Wikipedia
import info.anodsplace.weblists.db.WebListsDb
import io.ktor.util.Identity.decode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppDatabase(private val db: WebListsDb) {
    val webSites = db.webListQueries

    suspend fun upsert(siteId: Long, site: WebSite, lists: List<WebList>): Long = withContext(Dispatchers.Default) {
        return@withContext upsert(site.copy(id = siteId)) { id ->
            lists.map { list -> list.copy(siteId = id) }
        }
    }

    suspend fun preload() = withContext(Dispatchers.Default) {
        val sites = loadSites()
        if (sites.isNotEmpty()) {
            return@withContext
        }
        upsert(
            WebSite(
                url = "https://matchtv.ru/tvguide",
                title = "match.tv"
            )
        ) { siteId -> MatchTv.sample(siteId) }
        upsert(
            WebSite(
                url = "https://en.wikipedia.org/wiki/Main_Page",
                title = "Wikipedia"
            )
        ) { siteId -> Wikipedia.sample(siteId) }
    }

    suspend fun upsert(site: WebSite, lists: (siteId: Long) -> List<WebList>): Long = withContext(Dispatchers.Default){
        return@withContext db.transactionWithResult {
            var siteId = site.id
            if (siteId == 0L) {
                db.webSiteQueries.insert(
                    site.url, site.title
                )
                siteId = db.webSiteQueries.lastInsertId().executeAsOne()
            } else {
                db.webListQueries.delete(siteId = siteId)
                db.webSiteQueries.update(
                    siteId = siteId,
                    url = site.url,
                    title = site.title
                )
            }
            val entities = lists(siteId)
            entities.forEach {
                val transformation = it.apply.encode()
                db.webListQueries.insert(
                    it.siteId, it.order.toLong(), it.cssQuery, it.horizontal.toLong(), transformation
                )
            }
            return@transactionWithResult siteId
        }
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