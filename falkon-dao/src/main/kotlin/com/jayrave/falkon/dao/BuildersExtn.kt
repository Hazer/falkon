package com.jayrave.falkon.dao

import com.jayrave.falkon.engine.safeCloseAfterExecution
import com.jayrave.falkon.mapper.Column
import java.sql.SQLException

// ------------------------------------------ Insert -----------------------------------------------

/**
 * @return `true` if the insertion was successful; `false` otherwise
 */
fun <T : Any> com.jayrave.falkon.dao.insert.AdderOrEnder<T>.insert(): Boolean {
    return compile().safeCloseAfterExecution() == 1
}

// ------------------------------------------ Insert -----------------------------------------------


// ------------------------------------------ Update -----------------------------------------------

/**
 * @return number of rows affected by this update operation
 */
fun <T : Any> com.jayrave.falkon.dao.update.AdderOrEnder<T>.update(): Int {
    return compile().safeCloseAfterExecution()
}

/**
 * @return number of rows affected by this update operation
 */
fun <T : Any> com.jayrave.falkon.dao.update.PredicateAdderOrEnder<T>.update(): Int {
    return compile().safeCloseAfterExecution()
}

// ------------------------------------------ Update -----------------------------------------------


// ------------------------------------------ Delete -----------------------------------------------

/**
 * @return number of rows affected by this delete operation
 */
fun <T : Any> com.jayrave.falkon.dao.delete.DeleteBuilder<T>.delete(): Int {
    return compile().safeCloseAfterExecution()
}

/**
 * @return number of rows affected by this delete operation
 */
fun <T : Any> com.jayrave.falkon.dao.delete.AdderOrEnder<T>.delete(): Int {
    return compile().safeCloseAfterExecution()
}

// ------------------------------------------ Delete -----------------------------------------------


// ------------------------------------------- Query -----------------------------------------------

// A #query convenience function is not included here as it doesn't make sense to. Source that is
// returned from a CompiledQuery could end up not working if the CompiledQuery itself is closed

/**
 * A convenience function to select a list of columns
 * *Beware:* Every time this method is called an array as long as the passed in list is created
 */
fun <T : Any, Z : com.jayrave.falkon.dao.query.AdderOrEnder<T, Z>>
        com.jayrave.falkon.dao.query.AdderOrEnder<T, Z>.select(
        columns: List<Column<T, *>>): Z {

    return when (columns.isEmpty()) {
        true -> throw SQLException("Columns can't be empty")
        else -> {
            val columnsCount = columns.size
            when (columnsCount == 1) {
                true -> this.select(columns.first())
                else -> {
                    val firstColumn = columns.first()
                    val remainingColumns = Array(columnsCount - 1) { columns[it + 1] }
                    this.select(firstColumn, *remainingColumns)
                }
            }
        }
    }
}


/**
 * A convenience function to select a list of columns
 * *Beware:* Every time this method is called an array as long as the passed in list is created
 */
fun <Z : com.jayrave.falkon.dao.query.lenient.AdderOrEnder<Z>>
        com.jayrave.falkon.dao.query.lenient.AdderOrEnder<Z>.select(
        columns: List<Column<*, *>>): Z {

    return when (columns.isEmpty()) {
        true -> throw SQLException("Columns can't be empty")
        else -> {
            val columnsCount = columns.size
            when (columnsCount == 1) {
                true -> this.select(columns.first())
                else -> {
                    val firstColumn = columns.first()
                    val remainingColumns = Array(columnsCount - 1) { columns[it + 1] }
                    this.select(firstColumn, *remainingColumns)
                }
            }
        }
    }
}

// ------------------------------------------- Query -----------------------------------------------