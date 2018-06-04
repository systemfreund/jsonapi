package com.systemfreund

import com.systemfreund.Document.Companion.Data
import com.systemfreund.JsonApiObject.JsObjectValue
import com.systemfreund.JsonApiObject.Value
import java.math.BigDecimal

typealias Attributes = List<Attribute>
typealias Relationships = Map<String, Relationship>
typealias Links = Iterable<Link>
typealias Meta = Map<String, Value>
typealias Errors = List<Error>
typealias JsonApiInfo = List<JsonApiEntry>

data class Document(
        val data: Data? = null,
        val errors: Errors? = null,
        val links: Links? = null,
        val meta: Meta? = null,
        val included: ResourceObjects? = null,
        val jsonapi: JsonApiInfo? = null
) {
    companion object {
        interface Data

        data class ResourceObject(val id: String? = null,
                                  val type: String,
                                  val attributes: JsObjectValue? = null,
                                  val relationships: Relationships? = null,
                                  val links: Links? = null,
                                  val meta: Meta? = null
        ) : Data

        data class ResourceObjects(val array: List<ResourceObject>) : Data
    }
}

data class Attribute(val name: String, val value: Value)

data class Relationship(val links: Links? = null,
                        val data: Data? = null,
                        val meta: Meta? = null)

data class Link(val name: String, val url: String, val meta: Meta?) {
    companion object {
        fun self(url: String, meta: Meta? = null) = Link("self", url, meta)
        fun next(url: String, meta: Meta? = null) = Link("next", url, meta)
        fun prev(url: String, meta: Meta? = null) = Link("prev", url, meta)
        fun first(url: String, meta: Meta? = null) = Link("first", url, meta)
        fun last(url: String, meta: Meta? = null) = Link("last", url, meta)
        fun related(url: String, meta: Meta? = null) = Link("related", url, meta)
        fun about(url: String, meta: Meta? = null) = Link("about", url, meta)
    }
}


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
