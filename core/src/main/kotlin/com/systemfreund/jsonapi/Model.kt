package com.systemfreund.jsonapi

import com.systemfreund.jsonapi.JsonApiObject.JsObjectValue
import com.systemfreund.jsonapi.JsonApiObject.Value
import java.math.BigDecimal

typealias Attributes = List<Attribute>
typealias Relationships = Map<String, Relationship>
typealias Links = Map<String, Link>
typealias Meta = Map<String, Value>
typealias Errors = List<Error>
typealias JsonApiInfo = List<JsonApiEntry>

/**
 * Resource Identifier object.
 * [https://jsonapi.org/format/#document-resource-identifier-objects]
 */
data class ResourceIdentifier(val id: String, val type: String, val meta: Meta? = null)

data class Document(
        val data: Data? = null,
        val errors: Errors? = null,
        val links: Links? = null,
        val meta: Meta? = null,
        val included: ResourceObjects? = null,
        val jsonapi: JsonApiInfo? = null
) {
    companion object {
        interface Data : Iterable<ResourceObject>, Sequence<ResourceObject>

        data class ResourceObject(val id: String? = null,
                                  val type: String,
                                  val attributes: JsObjectValue? = null,
                                  val relationships: Relationships = emptyMap(),
                                  val links: Links? = null,
                                  val meta: Meta? = null
        ) : Data {
            val resourceIdentifier = id?.let { ResourceIdentifier(it, type, meta) }

            override fun iterator() = listOf(this).iterator()
        }

        data class ResourceObjects(val array: List<ResourceObject>) : Data {
            override fun iterator() = array.iterator()
        }
    }
}

data class Attribute(val name: String, val value: Value)

// data absent

data class Relationship(val links: Links? = null,

                        /**
                         * The resource linkage.
                         * [https://jsonapi.org/format/#document-resource-object-linkage]
                         */
                        val data: ResourceLinkage? = null,

                        val meta: Meta? = null)

sealed class ResourceLinkage : Iterable<ResourceIdentifier> {
    data class ToOneRelationship(val id: ResourceIdentifier? = null) : ResourceLinkage() {
        override fun iterator() = iterator { id?.let { yield(it) } }
    }

    data class ToManyRelationship(val ids: List<ResourceIdentifier> = emptyList())
        : ResourceLinkage(), Iterable<ResourceIdentifier> by ids

    fun isEmpty(): Boolean = iterator().hasNext()
}

data class Link(val href: String, val meta: Meta? = null)

data class Error(val id: String? = null,
                 val links: Links? = null,
                 val status: String? = null,
                 val code: String? = null,
                 val title: String? = null,
                 val detail: String? = null,
                 val source: ErrorSource? = null,
                 val meta: Meta? = null)

data class ErrorSource(val pointer: String? = null,
                       val parameter: String? = null)

data class JsonApiEntry(val name: String, val value: Value)

object JsonApiObject {
    interface Value

    data class StringValue(val value: String) : Value
    data class NumberValue(val value: BigDecimal) : Value
    data class BooleanValue(val value: Boolean) : Value
    data class JsObjectValue(val value: Attributes) : Value
    data class JsArrayValue(val value: List<Value>) : Value
    object NullValue : Value {
        override fun toString(): String {
            return "NullValue"
        }
    }

    val TrueValue = BooleanValue(true)
    val FalseValue = BooleanValue(false)
}
