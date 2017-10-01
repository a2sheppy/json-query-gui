package com.raybritton.jsonquery

import com.raybritton.jsonquery.models.Query

class QueryBuilder {
    var method: Query.Method? = null
    var target: String? = null
    var order: String? = null
    var extra: Query.TargetExtra? = null
    var keys = listOf<String>()
    var offset: Int? = null
    var limit: Int? = null
    var withKeys = false
    var asJson = false
    var pretty = false
    var desc = false
    var distinct = false
    var where: Query.Where? = null

    fun build(): Query {
        method?.let { method ->
            target?.let { target ->
                return internalBuild(method, target)
            } ?: throw NullPointerException("target was null")
        } ?: throw NullPointerException("method was null")
    }

    private fun internalBuild(method: Query.Method, target: String): Query {
        return Query(
                method = method,
                target = target,
                targetExtra = extra,
                targetKeys = keys,
                offset = offset,
                limit = limit,
                asJson = asJson,
                pretty = pretty,
                desc = desc,
                distinct = distinct,
                withKeys = withKeys,
                order = order,
                where = where
        )
    }
}
