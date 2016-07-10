package com.jayrave.falkon.dao

import com.jayrave.falkon.engine.safeCloseAfterExecution

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


// A #query convenience function is not included here as it doesn't make sense to. Source that is
// returned from a CompiledQuery could end up not working if the CompiledQuery itself is closed