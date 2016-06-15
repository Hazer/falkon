package com.jayrave.falkon.dao.testLib

import com.jayrave.falkon.engine.*
import com.nhaarman.mockito_kotlin.mock
import java.util.*

/**
 * [CompiledStatement]s for which bindings can't be cleared
 */
internal open class OneShotCompiledStatementForTest<R>(
        override val sql: String = "dummy", private val returnValue: R,
        private val shouldThrowOnExecution: Boolean = false) :
        CompiledStatement<R> {

    private val mutableBoundArgs = HashMap<Int, Any?>()

    val boundArgs: Map<Int, Any?>
        get() = mutableBoundArgs

    val wasExecutionAttempted: Boolean
        get() = numberOfTimesExecutionAttempted > 0

    val isExecuted: Boolean
        get() = numberOfTimesExecuted > 0

    var numberOfTimesExecutionAttempted: Int = 0
        private set

    var numberOfTimesExecuted: Int = 0
        private set

    var isClosed = false
        private set

    override fun execute(): R {
        numberOfTimesExecutionAttempted++

        // Check and throw exception if required
        if (shouldThrowOnExecution) {
            throw RuntimeException()
        }

        numberOfTimesExecuted++
        return returnValue
    }

    override fun bindShort(index: Int, value: Short?): CompiledStatement<R> {
        mutableBoundArgs[index] = value
        return this
    }

    override fun bindInt(index: Int, value: Int?): CompiledStatement<R> {
        mutableBoundArgs[index] = value
        return this
    }

    override fun bindLong(index: Int, value: Long?): CompiledStatement<R> {
        mutableBoundArgs[index] = value
        return this
    }

    override fun bindFloat(index: Int, value: Float?): CompiledStatement<R> {
        mutableBoundArgs[index] = value
        return this
    }

    override fun bindDouble(index: Int, value: Double?): CompiledStatement<R> {
        mutableBoundArgs[index] = value
        return this
    }

    override fun bindString(index: Int, value: String?): CompiledStatement<R> {
        mutableBoundArgs[index] = value
        return this
    }

    override fun bindBlob(index: Int, value: ByteArray?): CompiledStatement<R> {
        mutableBoundArgs[index] = value
        return this
    }

    override fun close() {
        when {
            !isClosed -> isClosed = true
            else -> throw RuntimeException("Trying to close statement that's already closed")
        }
    }

    override fun clearBindings(): CompiledStatement<R> {
        throw RuntimeException(
                "${OneShotCompiledStatementForTest::class.java.canonicalName} " +
                        "doesn't support clearing bindings"
        )
    }

    fun shortBoundAt(index: Int): Short = mutableBoundArgs[index] as Short
    fun intBoundAt(index: Int): Int = mutableBoundArgs[index] as Int
    fun longBoundAt(index: Int): Long = mutableBoundArgs[index] as Long
    fun floatBoundAt(index: Int): Float = mutableBoundArgs[index] as Float
    fun doubleBoundAt(index: Int): Double = mutableBoundArgs[index] as Double
    fun stringBoundAt(index: Int): String = mutableBoundArgs[index] as String
    fun blobBoundAt(index: Int): ByteArray = mutableBoundArgs[index] as ByteArray
    fun isNullBoundAt(index: Int): Boolean = mutableBoundArgs[index] == null
}


internal class OneShotCompiledInsertForTest(
        sql: String, returnValue: Int = 1, shouldThrowOnExecution: Boolean = false) :
        CompiledInsert,
        OneShotCompiledStatementForTest<Int>(sql, returnValue, shouldThrowOnExecution)


internal class OneShotCompiledUpdateForTest(
        sql: String, returnValue: Int = 1, shouldThrowOnExecution: Boolean = false) :
        CompiledUpdate,
        OneShotCompiledStatementForTest<Int>(sql, returnValue, shouldThrowOnExecution)


internal class OneShotCompiledDeleteForTest(
        sql: String, returnValue: Int = 1, shouldThrowOnExecution: Boolean = false) :
        CompiledDelete,
        OneShotCompiledStatementForTest<Int>(sql, returnValue, shouldThrowOnExecution)


internal class OneShotCompiledQueryForTest(
        sql: String, returnValue: Source = mock(), shouldThrowOnExecution: Boolean = false) :
        CompiledQuery,
        OneShotCompiledStatementForTest<Source>(sql, returnValue, shouldThrowOnExecution)