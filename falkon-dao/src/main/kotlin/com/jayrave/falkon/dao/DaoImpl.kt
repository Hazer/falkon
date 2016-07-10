package com.jayrave.falkon.dao

import com.jayrave.falkon.Table
import com.jayrave.falkon.dao.delete.DeleteBuilder
import com.jayrave.falkon.dao.delete.DeleteBuilderImpl
import com.jayrave.falkon.dao.insert.InsertBuilder
import com.jayrave.falkon.dao.insert.InsertBuilderImpl
import com.jayrave.falkon.dao.query.QueryBuilder
import com.jayrave.falkon.dao.query.QueryBuilderImpl
import com.jayrave.falkon.dao.update.UpdateBuilder
import com.jayrave.falkon.dao.update.UpdateBuilderImpl
import com.jayrave.falkon.sqlBuilders.DeleteSqlBuilder
import com.jayrave.falkon.sqlBuilders.InsertSqlBuilder
import com.jayrave.falkon.sqlBuilders.QuerySqlBuilder
import com.jayrave.falkon.sqlBuilders.UpdateSqlBuilder

open class DaoImpl<T : Any, ID : Any>(
        override val table: Table<T, ID>,
        private val configuration: Configuration,
        private val insertSqlBuilder: InsertSqlBuilder,
        private val updateSqlBuilder: UpdateSqlBuilder,
        private val deleteSqlBuilder: DeleteSqlBuilder,
        private val querySqlBuilder: QuerySqlBuilder) : Dao<T, ID> {

    override final fun insertBuilder(): InsertBuilder<T> = InsertBuilderImpl(
            table, insertSqlBuilder, configuration.argPlaceholder
    )

    override final fun updateBuilder(): UpdateBuilder<T> = UpdateBuilderImpl(
            table, updateSqlBuilder, configuration.argPlaceholder
    )

    override final fun deleteBuilder(): DeleteBuilder<T>  = DeleteBuilderImpl(
            table, deleteSqlBuilder, configuration.argPlaceholder
    )

    override final fun queryBuilder(): QueryBuilder<T> = QueryBuilderImpl(
            table, querySqlBuilder, configuration.argPlaceholder,
            configuration.orderByAscendingKey, configuration.orderByDescendingKey
    )
}