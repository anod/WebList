package info.anodsplace.weblists.rules

import androidx.room.*
import info.anodsplace.weblists.samples.MatchTv
import info.anodsplace.weblists.samples.Wikipedia

@Dao
interface WebSiteStore {

    @Transaction
    @Query("SELECT * FROM web_site WHERE id IN (:siteId)")
    suspend fun loadById(siteId: Long): WebSiteLists

    @Query("SELECT * FROM web_site")
    suspend fun loadSites(): List<WebSite>

    @Insert
    suspend fun insert(entity: WebSite): Long

    @Update
    suspend fun update(entity: WebSite)

    @Insert
    suspend fun insert(entity: WebList): Long

    @Update
    suspend fun update(entity: WebList)

    @Query("DELETE FROM web_site")
    suspend fun deleteSites()

    @Transaction
    suspend fun insert(site: WebSite, lists: (siteId: Long) -> List<WebList>) {
        val siteId = insert(site)
        val entities = lists(siteId)
        entities.forEach {
            insert(it)
        }
    }

    suspend fun preload() {
        val sites = loadSites()
        if (sites.isNotEmpty()) {
            deleteSites()
        }
        insert(WebSite(
            url = "https://matchtv.ru/tvguide",
            title = "match.tv"
        )) { siteId -> MatchTv.sample(siteId) }
        insert(WebSite(
            url = "https://en.wikipedia.org/wiki/Main_Page",
            title = "Wikipedia"
        )) { siteId -> Wikipedia.sample(siteId) }

    }
}
