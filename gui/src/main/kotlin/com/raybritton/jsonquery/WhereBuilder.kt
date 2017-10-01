package com.raybritton.jsonquery

import com.raybritton.jsonquery.models.Query

class WhereBuilder {
    var field: String? = null
    var operator: Query.Where.Operator? = null
    var compare: String? = null

    fun build(): Query.Where {
        field?.let { field ->
            operator?.let { operator ->
                return internalBuild(field, operator)
            } ?: throw NullPointerException("operator was null")
        } ?: throw NullPointerException("field was null")
    }

    private fun internalBuild(field: String, operator: Query.Where.Operator): Query.Where {
        return Query.Where(field, operator, compare)
    }
}