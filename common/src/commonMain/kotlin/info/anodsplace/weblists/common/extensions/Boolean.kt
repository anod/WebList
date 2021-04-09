package info.anodsplace.weblists.common.extensions

fun Boolean.toLong(): Long = if (this) 1L else 0L

fun Long.toBool(): Boolean = this == 1L