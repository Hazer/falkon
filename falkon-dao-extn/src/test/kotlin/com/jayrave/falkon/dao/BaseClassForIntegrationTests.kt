package com.jayrave.falkon.dao

import com.jayrave.falkon.dao.testLib.ModelForTest
import com.jayrave.falkon.dao.testLib.TableForTest
import com.jayrave.falkon.dao.testLib.defaultTableConfiguration
import com.jayrave.falkon.engine.*
import com.jayrave.falkon.engine.jdbc.JdbcEngineCore
import org.assertj.core.api.Assertions.assertThat
import org.h2.jdbcx.JdbcConnectionPool
import org.junit.After
import org.junit.Before
import java.util.*
import javax.sql.DataSource

/**
 * Before every test, a clean database that just has an empty `test` table that corresponds to
 * [TableForTest] is setup
 */
abstract class BaseClassForIntegrationTests {

    private lateinit var dataSource: DataSource
    protected lateinit var engine: Engine
    internal lateinit var table: TableForTest

    @Before
    fun setUp() {
        // http://www.h2database.com/html/features.html#in_memory_databases
        // Give the database a name to enabled multiple connections to the same database
        val ds = JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "pw")
        ds.loginTimeout = 1
        ds.maxConnections = 1

        dataSource = ds
        engine = DefaultEngine(JdbcEngineCore(dataSource))
        table = TableForTest(defaultTableConfiguration(engine))

        engine.createTableForTest(table)
    }


    @After
    fun tearDown() {
        // http://www.h2database.com/html/grammar.html#shutdown
        // http://www.h2database.com/html/features.html#in_memory_databases
        // By default h2 closes the database when all existing connections to it are closed.
        // For an in-memory db, closing is akin to nuking it. This makes sure that we have a
        // clean slate for every test
        dataSource.connection.prepareStatement("SHUTDOWN").execute()
    }


    companion object {

        // -------------------------------- Using hand-built SQL -----------------------------------

        private fun Engine.createTableForTest(table: TableForTest) {
            this.compileSql(
                    listOf(table.name),
                    "CREATE TABLE ${table.name} (" +
                            "${table.id1.name} VARCHAR NOT NULL, " +
                            "${table.id2.name} VARCHAR NOT NULL, " +
                            "${table.short.name} SMALLINT NOT NULL, " +
                            "${table.int.name} INTEGER NOT NULL, " +
                            "${table.long.name} BIGINT NOT NULL, " +
                            "${table.float.name} REAL NOT NULL, " +
                            "${table.double.name} DOUBLE NOT NULL, " +
                            "${table.string.name} VARCHAR NOT NULL, " +
                            "${table.blob.name} BLOB NOT NULL, " +
                            "${table.nullableShort.name} SMALLINT, " +
                            "${table.nullableInt.name} INTEGER, " +
                            "${table.nullableLong.name} BIGINT, " +
                            "${table.nullableFloat.name} REAL, " +
                            "${table.nullableDouble.name} DOUBLE, " +
                            "${table.nullableString.name} VARCHAR, " +
                            "${table.nullableBlob.name} BLOB, " +
                            "PRIMARY KEY(${table.id1.name}, ${table.id2.name}))"
            ).safeCloseAfterExecution()
        }


        /**
         * @param seedValue will be used as is for short & every subsequent parameter will
         * be 1 more than the previous parameter
         */
        internal fun buildModelForTest(
                seedValue: Short, id1: UUID = UUID.randomUUID(), id2: UUID = UUID.randomUUID()):
                ModelForTest {

            return ModelForTest(
                    id1 = id1, id2 = id2, short = seedValue, int = seedValue + 1,
                    long = seedValue + 2L, float = seedValue + 3F, double = seedValue + 4.0,
                    string = "test ${seedValue + 5}", blob = byteArrayOf((seedValue + 6).toByte())
            )
        }


        internal fun getNumberOfModelsInTableForTest(table: TableForTest): Int {
            val compiledStatementForQuery = table
                    .configuration
                    .engine
                    .compileQuery(listOf(table.name), "SELECT COUNT(*) as count from ${table.name}")

            val source = compiledStatementForQuery.execute()
            assertThat(source.moveToNext()).isTrue()
            val count = source.getInt(source.getColumnIndex("count"))

            source.close()
            compiledStatementForQuery.close()
            return count
        }


        internal fun assertPresenceOf(table: TableForTest, vararg models: ModelForTest) {
            models.forEach {
                val compiledStatementForQuery = compileQueryFor(table, it)
                val source = compiledStatementForQuery.execute()

                assertThat(source.moveToNext()).isTrue()
                assertCurrentRowCorrespondsTo(source, it, table)
                assertThat(source.moveToNext()).isFalse()

                source.close()
                compiledStatementForQuery.close()
            }
        }


        internal fun assertAbsenceOf(table: TableForTest, vararg models: ModelForTest) {
            models.forEach {
                val compiledStatementForQuery = compileQueryFor(table, it)
                val source = compiledStatementForQuery.execute()

                val isPresent = when (source.moveToNext()) {
                    true -> rowCorrespondsTo(source, it, table)
                    else -> false
                }

                assertThat(isPresent).isFalse()
                source.close()
                compiledStatementForQuery.close()
            }
        }


        internal fun assertCurrentRowCorrespondsTo(s: Source, m: ModelForTest, t: TableForTest) {
            assertThat(rowCorrespondsTo(s, m, t)).isTrue()
        }


        private fun compileQueryFor(
                table: TableForTest, model: ModelForTest):
                CompiledStatement<Source> {

            val storageFormOfId1 = table.id1.computeStorageFormOf(
                    table.id1.extractPropertyFrom(model)
            ) as String

            val storageFormOfId2 = table.id2.computeStorageFormOf(
                    table.id2.extractPropertyFrom(model)
            ) as String

            val sql = "SELECT * FROM ${table.name} WHERE " +
                    "${table.id1.name} = ? AND ${table.id2.name} = ?"

            return table
                    .configuration
                    .engine
                    .compileQuery(listOf(table.name), sql)
                    .bind(1, storageFormOfId1)
                    .bind(2, storageFormOfId2)
        }


        private fun rowCorrespondsTo(s: Source, m: ModelForTest, t: TableForTest): Boolean {
            return s.getString(s.getColumnIndex(t.id1.name)) == m.id1.toString() &&
                    s.getString(s.getColumnIndex(t.id2.name)) == m.id2.toString() &&
                    s.getShort(s.getColumnIndex(t.short.name)) == m.short &&
                    s.getInt(s.getColumnIndex(t.int.name)) == m.int &&
                    s.getLong(s.getColumnIndex(t.long.name)) == m.long &&
                    s.getFloat(s.getColumnIndex(t.float.name)) == m.float &&
                    s.getDouble(s.getColumnIndex(t.double.name)) == m.double &&
                    s.getString(s.getColumnIndex(t.string.name)) == m.string &&
                    Arrays.equals(s.getBlob(s.getColumnIndex(t.blob.name)), m.blob) &&

                    ((s.isNull(s.getColumnIndex(
                            t.nullableShort.name)) && m.nullableShort == null) || (s.getShort(
                            s.getColumnIndex(t.nullableShort.name)) == m.nullableShort)) &&

                    ((s.isNull(s.getColumnIndex(
                            t.nullableInt.name)) && m.nullableInt == null) || (s.getInt(
                            s.getColumnIndex(t.nullableInt.name)) == m.nullableInt)) &&

                    ((s.isNull(s.getColumnIndex(
                            t.nullableLong.name)) && m.nullableLong == null) || (s.getLong(
                            s.getColumnIndex(t.nullableLong.name)) == m.nullableLong)) &&

                    ((s.isNull(s.getColumnIndex(
                            t.nullableFloat.name)) && m.nullableFloat == null) || (s.getFloat(
                            s.getColumnIndex(t.nullableFloat.name)) == m.nullableFloat)) &&

                    ((s.isNull(s.getColumnIndex(
                            t.nullableDouble.name)) && m.nullableDouble == null) || (s.getDouble(
                            s.getColumnIndex(t.nullableDouble.name)) == m.nullableDouble)) &&

                    ((s.isNull(s.getColumnIndex(
                            t.nullableString.name)) && m.nullableString == null) || (s.getString(
                            s.getColumnIndex(t.nullableString.name)) == m.nullableString)) &&

                    ((s.isNull(s.getColumnIndex(
                            t.nullableBlob.name)) && m.nullableBlob == null) || (Arrays.equals(
                            s.getBlob(s.getColumnIndex(t.nullableBlob.name)), m.nullableBlob)))
        }

        // -------------------------------- Using hand-built SQL -----------------------------------


        // ----------------------------------- Using builders --------------------------------------

        internal fun insertModelUsingInsertBuilder(table: TableForTest, model: ModelForTest) {
            table.dao.insertBuilder().values {
                set(table.id1, model.id1)
                set(table.id2, model.id2)
                set(table.short, model.short)
                set(table.int, model.int)
                set(table.long, model.long)
                set(table.float, model.float)
                set(table.double, model.double)
                set(table.string, model.string)
                set(table.blob, model.blob)
                set(table.nullableShort, model.nullableShort)
                set(table.nullableInt, model.nullableInt)
                set(table.nullableLong, model.nullableLong)
                set(table.nullableFloat, model.nullableFloat)
                set(table.nullableDouble, model.nullableDouble)
                set(table.nullableString, model.nullableString)
                set(table.nullableBlob, model.nullableBlob)
            }.insert()
        }


        internal fun insertModelsUsingInsertBuilder(
                table: TableForTest, vararg models: ModelForTest) {

            table.configuration.engine.executeInTransaction {
                models.forEach { insertModelUsingInsertBuilder(table, it) }
            }
        }


        internal fun insertAdditionalRandomModelsUsingInsertBuilder(
                table: TableForTest, count: Int) {

            (0..count - 1).forEach {
                insertModelUsingInsertBuilder(table, buildModelForTest(seedValue = it.toShort()))
            }
        }

        // ----------------------------------- Using builders --------------------------------------
    }
}