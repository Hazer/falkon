package com.jayrave.falkon.dao

import com.jayrave.falkon.engine.CompiledStatement
import com.jayrave.falkon.mapper.Column

fun <T : Any, ID : Any> Dao<T, ID>.insert(t: T) {
    insert(listOf(t))
}


fun <T : Any, ID : Any> Dao<T, ID>.insert(vararg ts: T) {
    insert(ts.asList())
}


fun <T : Any, ID : Any> Dao<T, ID>.insert(ts: Iterable<T>) {
    val allColumns = table.allColumns
    throwIfColumnCollectionIsEmpty(allColumns)

    table.configuration.engine.executeInTransaction {
        var compiledStatementForInsert: CompiledStatement<Int>? = null
        try {
            for (item in ts) {
                compiledStatementForInsert = when (compiledStatementForInsert) {

                    // First item. Build CompiledStatement for insert
                    null -> buildCompiledStatementForInsert(item, allColumns)

                    // Not the first item. Clear bindings & rebind all columns
                    else -> {
                        compiledStatementForInsert.clearBindings()
                        compiledStatementForInsert.bindColumns(allColumns, item)
                        compiledStatementForInsert
                    }
                }

                compiledStatementForInsert.execute()
            }
        } finally {
            // No matter what happens, CompiledStatement must be closed
            // to prevent resource leakage
            compiledStatementForInsert?.close()
        }
    }
}


/**
 * @param item Item to build [CompiledStatement] for
 * @param columns list of non empty columns with deterministic iteration order
 *
 * @return [CompiledStatement] corresponding to the passed in [item]
 * @throws IllegalArgumentException if the passed in [columns] is empty
 */
private fun <T : Any> Dao<T, *>.buildCompiledStatementForInsert(
        item: T, columns: Collection<Column<T, *>>): CompiledStatement<Int> {

    throwIfColumnCollectionIsEmpty(columns)

    return insertBuilder().values {
        columns.forEach { it ->

            @Suppress("UNCHECKED_CAST")
            val column = it as Column<T, Any?>
            set(column, column.extractPropertyFrom(item))
        }
    }.compile()
}


private fun throwIfColumnCollectionIsEmpty(columns: Collection<*>) {
    if (columns.isEmpty()) {
        throw IllegalArgumentException("Columns can't be empty for inserts")
    }
}