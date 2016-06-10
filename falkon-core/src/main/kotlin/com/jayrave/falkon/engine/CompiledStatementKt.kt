package com.jayrave.falkon.engine

/**
 * [Short], [Int], [Long], [Float], [Double], [ByteArray] & null will be bound by calling the
 * appropriate `bind` method on [CompiledStatement]. Every other argument will be bound as [String]
 *
 * @param index the 1-based index where [value] will be bound
 * @param value the parameter value
 */
fun <R> CompiledStatement<R>.bind(index: Int, value: Any?): CompiledStatement<R> {
    when (value) {
        null -> bindNull(index)
        is Short -> bindShort(index, value)
        is Int -> bindInt(index, value)
        is Long -> bindLong(index, value)
        is Float -> bindFloat(index, value)
        is Double -> bindDouble(index, value)
        is ByteArray -> bindBlob(index, value)
        else -> bindString(index, value.toString())
    }

    return this
}


/**
 * All arguments will be bound according to the semantics of [bind]
 *
 * @param values to be bound
 * @param startIndex the 1-based index from where binding should be started
 */
fun <R> CompiledStatement<R>.bindAll(values: Iterable<Any?>?, startIndex: Int = 1):
        CompiledStatement<R> {

    values?.forEachIndexed { index, value ->
        bind(startIndex + index, value)
    }

    return this
}


/**
 * Executes the statement, closes it and returns the result of execution
 */
fun <R> CompiledStatement<R>.executeAndClose(): R {
    try {
        return execute()
    } finally {
        close()
    }
}