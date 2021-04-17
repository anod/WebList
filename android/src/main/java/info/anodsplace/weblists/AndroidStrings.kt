package info.anodsplace.weblists

import android.content.Context
import info.anodsplace.weblists.common.StringProvider

class AndroidStrings(private val context: Context) : StringProvider {
    override val edit: String
        get() = context.getString(R.string.edit)
    override val appName: String
        get() = context.getString(R.string.app_name)
    override val search: String
        get() = context.getString(R.string.search)
    override val previousResult: String
        get() = context.getString(R.string.previous_result)
    override val nextResult: String
        get() = context.getString(R.string.next_result)
    override val findText: String
        get() = context.getString(R.string.find_text)
    override val import: String
        get() = context.getString(R.string.import_button)
    override val export: String
        get() = context.getString(R.string.export)
    override val backToCatalog: String
        get() = context.getString(R.string.back_to_catalog)
    override val addNew: String
        get() = context.getString(R.string.add_new)
    override val noListsFound: String
        get() = context.getString(R.string.no_lists_found)
}
