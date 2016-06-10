package com.jayrave.falkon.dao.delete

import com.jayrave.falkon.Table
import com.jayrave.falkon.dao.where.AfterSimpleConnectorAdder
import com.jayrave.falkon.dao.where.Where
import com.jayrave.falkon.dao.where.WhereBuilder
import com.jayrave.falkon.dao.where.WhereBuilderImpl
import com.jayrave.falkon.engine.bindAll
import com.jayrave.falkon.engine.compileDelete
import com.jayrave.falkon.engine.executeAndClose

internal class DeleteBuilderImpl<T : Any>(override val table: Table<T, *, *>) : DeleteBuilder<T> {

    private var whereBuilder: WhereBuilderImpl<T, AdderOrEnder<T>>? = null

    override fun where(): WhereBuilder<T, AdderOrEnder<T>> {
        whereBuilder = WhereBuilderImpl { AdderOrEnderImpl(it) }
        return whereBuilder!!
    }

    override fun delete(): Int {
        val where: Where? = whereBuilder?.build()
        return table.configuration.engine
                .compileDelete(table.name, where?.clause)
                .bindAll(where?.arguments)
                .executeAndClose()
    }


    private inner class AdderOrEnderImpl(
            private val delegate: WhereBuilderImpl<T, AdderOrEnder<T>>) :
            AdderOrEnder<T> {

        override fun and(): AfterSimpleConnectorAdder<T, AdderOrEnder<T>> {
            delegate.and()
            return delegate
        }

        override fun or(): AfterSimpleConnectorAdder<T, AdderOrEnder<T>> {
            delegate.or()
            return delegate
        }

        override fun delete(): Int {
            return this@DeleteBuilderImpl.delete()
        }
    }
}