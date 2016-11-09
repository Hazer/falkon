package com.jayrave.falkon.sqlBuilders.test.create

import com.jayrave.falkon.sqlBuilders.CreateTableSqlBuilder
import com.jayrave.falkon.sqlBuilders.test.DbForTest
import com.jayrave.falkon.sqlBuilders.test.buildArgListForSql
import com.jayrave.falkon.sqlBuilders.test.failIfOpDoesNotThrow
import com.jayrave.falkon.sqlBuilders.test.randomUuid
import org.assertj.core.api.Assertions.assertThat
import java.util.*

class TestCompositePrimaryKey(
        createTableSqlBuilder: CreateTableSqlBuilder,
        private val db: DbForTest) {

    init {
        val id1ColumnInfo = ColumnInfoForTest("id1", dataType = db.stringDataType, isId = true)
        val id2ColumnInfo = ColumnInfoForTest("id2", dataType = db.stringDataType, isId = true)
        val tableInfo = TableInfoForTest(
                TABLE_NAME, listOf(id1ColumnInfo, id2ColumnInfo), emptyList(), emptyList()
        )

        db.execute(createTableSqlBuilder.build(tableInfo))
    }


    fun `composite primary key does not allow duplicates`() {
        // Make sure records can be inserted
        val id1ToBeDuplicated = randomUuid()
        val id2ToBeDuplicated = randomUuid()
        db.execute(buildInsertSql(id1ToBeDuplicated, id2ToBeDuplicated))
        db.execute(buildInsertSql(id1ToBeDuplicated, randomUuid()))
        db.execute(buildInsertSql(randomUuid(), id2ToBeDuplicated))
        assertRowCountInTable(3)

        // Try inserting another row with the same id. It should throw
        failIfOpDoesNotThrow {
            db.execute(buildInsertSql(id1ToBeDuplicated, id2ToBeDuplicated))
        }
    }


    fun `composite primary key does not allow any attributes to be null`() {
        // Make sure records can be inserted
        db.execute(buildInsertSql(randomUuid(), randomUuid()))
        assertRowCountInTable(1)

        // Try inserting row with one of the id attributes being null. It should throw
        failIfOpDoesNotThrow {
            db.execute(buildInsertSql(null, randomUuid()))
        }
    }


    private fun assertRowCountInTable(expected: Int) {
        assertThat(db.findRecordCountInTable(TABLE_NAME)).isEqualTo(expected)
    }



    companion object {
        private const val TABLE_NAME = "test"
        private fun buildInsertSql(id1Value: UUID?, id2Value: UUID?): String {
            return "INSERT INTO $TABLE_NAME VALUES (${buildArgListForSql(id1Value, id2Value)})"
        }
    }
}