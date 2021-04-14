package info.anodsplace.weblists.common

interface StringProvider {

    val appName: String
    val search: String
    val previousResult: String
    val nextResult: String
    val findText: String
    val import: String
    val export: String
    val backToCatalog: String
    val addNew: String
    val noListsFound: String

    class Default() : StringProvider {
        override val appName = "Web lists"
        override val search = "Search"
        override val previousResult = "Previous result"
        override val nextResult = "Next result"
        override val findText = "Find text"
        override val import = "Import"
        override val export = "Export"
        override val backToCatalog = "Back"
        override val addNew = "Add new"
        override val noListsFound = "No lists found"
    }
}