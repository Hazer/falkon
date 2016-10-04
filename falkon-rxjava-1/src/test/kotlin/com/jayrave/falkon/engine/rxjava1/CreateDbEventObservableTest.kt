package com.jayrave.falkon.engine.rxjava1

import com.jayrave.falkon.engine.DbEvent
import com.jayrave.falkon.engine.DbEventListener
import com.jayrave.falkon.engine.Engine
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.CountDownLatch

class CreateDbEventObservableTest {

    private val engineForTest = EngineForTest()

    @Test
    fun testInsertDbEventsAreDelivered() {
        val insertEventToBeFired = DbEvent.forInsert("test")
        val caughtEvents = fireSingleEventAndCatchResult(insertEventToBeFired)
        assertThat(caughtEvents).hasSize(1)
        assertThat(caughtEvents.first()).isEqualTo(insertEventToBeFired)
    }


    @Test
    fun testUpdateDbEventsAreDelivered() {
        val updateEventToBeFired = DbEvent.forUpdate("test")
        val caughtEvents = fireSingleEventAndCatchResult(updateEventToBeFired)
        assertThat(caughtEvents).hasSize(1)
        assertThat(caughtEvents.first()).isEqualTo(updateEventToBeFired)
    }


    @Test
    fun testDeleteDbEventsAreDelivered() {
        val deleteEventToBeFired = DbEvent.forDelete("test")
        val caughtEvents = fireSingleEventAndCatchResult(deleteEventToBeFired)
        assertThat(caughtEvents).hasSize(1)
        assertThat(caughtEvents.first()).isEqualTo(deleteEventToBeFired)
    }


    @Test
    fun testAllEventsAreDeliveredWhenMultipleEventsAreFired() {
        val eventsToBeFired = listOf(
                DbEvent.forInsert("test_1"),
                DbEvent.forUpdate("test_2"),
                DbEvent.forDelete("test_3")
        )

        val countDownLatch = CountDownLatch(1)
        val caughtEvents = ArrayList<DbEvent>()
        engineForTest.createDefaultDbEventObservable().subscribe {
            caughtEvents.addAll(it)
            countDownLatch.countDown()
        }

        engineForTest.dbEventListeners.forEach { it.onEvents(eventsToBeFired) } // Fire events
        countDownLatch.awaitWithDefaultTimeout() // Wait for event to be caught

        // Assert caught events
        assertThat(caughtEvents).hasSameElementsAs(eventsToBeFired)
    }


    @Test
    fun testDbEventListenerIsUnregisteredOnUnsubscription() {
        assertThat(engineForTest.dbEventListeners).isEmpty()
        val subscription = engineForTest.createDefaultDbEventObservable().subscribe()
        assertThat(engineForTest.dbEventListeners).hasSize(1)
        subscription.unsubscribe()
        assertThat(engineForTest.dbEventListeners).isEmpty()
    }


    private fun fireSingleEventAndCatchResult(eventToBeFired: DbEvent): List<DbEvent> {
        val countDownLatch = CountDownLatch(1)
        val caughtEvents = ArrayList<DbEvent>()
        engineForTest.createDefaultDbEventObservable().subscribe {
            caughtEvents.addAll(it)
            countDownLatch.countDown()
        }

        engineForTest.dbEventListeners.forEach { it.onEvent(eventToBeFired) } // Fire event
        countDownLatch.awaitWithDefaultTimeout() // Wait for event to be caught
        return caughtEvents // Send back caught events
    }



    private class EngineForTest : Engine {

        val dbEventListeners = ArrayList<DbEventListener>()

        override fun <R> executeInTransaction(operation: () -> R) = throw exception()
        override fun isInTransaction() = throw exception()
        override fun compileSql(tableNames: Iterable<String>?, rawSql: String) = throw exception()
        override fun compileInsert(tableName: String, rawSql: String) = throw exception()
        override fun compileUpdate(tableName: String, rawSql: String) = throw exception()
        override fun compileDelete(tableName: String, rawSql: String) = throw exception()
        override fun compileQuery(tableNames: Iterable<String>, rawSql: String) = throw exception()
        private fun exception() = UnsupportedOperationException("not implemented")

        override fun registerDbEventListener(dbEventListener: DbEventListener) {
            dbEventListeners.add(dbEventListener)
        }

        override fun unregisterDbEventListener(dbEventListener: DbEventListener) {
            dbEventListeners.remove(dbEventListener)
        }
    }



    companion object {
        private fun Engine.createDefaultDbEventObservable(): Observable<Iterable<DbEvent>> {
            return createDbEventObservable(Schedulers.newThread())
        }
    }
}