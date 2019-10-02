@file:Suppress("unused")

package com.systemfreund.jsonapi

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.systemfreund.jsonapi.Document.Companion.Data
import com.systemfreund.jsonapi.Document.Companion.ResourceObject
import com.systemfreund.jsonapi.Document.Companion.ResourceObjects
import com.systemfreund.jsonapi.JsonApiObject.FalseValue
import com.systemfreund.jsonapi.JsonApiObject.JsArrayValue
import com.systemfreund.jsonapi.JsonApiObject.JsObjectValue
import com.systemfreund.jsonapi.JsonApiObject.NullValue
import com.systemfreund.jsonapi.JsonApiObject.NumberValue
import com.systemfreund.jsonapi.JsonApiObject.StringValue
import com.systemfreund.jsonapi.JsonApiObject.TrueValue
import com.systemfreund.jsonapi.JsonApiObject.Value
import com.systemfreund.jsonapi.ResourceLinkage.EmptyToOneRelationship
import com.systemfreund.jsonapi.ResourceLinkage.ToManyRelationship
import com.systemfreund.jsonapi.ResourceLinkage.ToOneRelationship

object JsonApiJackson {

    fun createMapper(): ObjectMapper = configureMapper(ObjectMapper())

    private fun configureMapper(mapper: ObjectMapper): ObjectMapper {
        mapper.registerKotlinModule()
        mapper.addMixIn(Document::class.java, DocumentMixin::class.java)
        mapper.addMixIn(ResourceObject::class.java, ResourceObjectMixin::class.java)
        mapper.addMixIn(ResourceObjects::class.java, ResourceObjectsMixin::class.java)
        mapper.addMixIn(Relationship::class.java, RelationshipMixin::class.java)
        mapper.addMixIn(Value::class.java, ValueMixin::class.java)
        mapper.addMixIn(JsObjectValue::class.java, JsObjectValueMixin::class.java)
        mapper.addMixIn(Error::class.java, ErrorMixin::class.java)
        return mapper
    }

}

//
// Mix-ins
//

sealed class DocumentMixin(
        @JsonDeserialize(using = DataDeserializer::class) val data: Data?,
        val errors: Errors?,
        @JsonDeserialize(using = LinksDeserializer::class) val links: Links?,
        val meta: Meta?,
        @JsonDeserialize(using = DataDeserializer::class) val included: ResourceObjects?,
        @JsonDeserialize(using = JsonApiInfoDeserializer::class) val jsonapi: JsonApiInfo?
)

sealed class ResourceObjectMixin(
        val id: String?,
        val type: String,
        val attributes: JsObjectValue?,
        @JsonDeserialize(using = RelationshipsDeserializer::class) val relationships: Relationships = emptyMap(),
        @JsonDeserialize(using = LinksDeserializer::class) val links: Links?,
        val meta: Meta? = null)

sealed class ResourceObjectsMixin(@JsonValue val array: List<ResourceObject>)

sealed class RelationshipMixin(
        @JsonDeserialize(using = LinksDeserializer::class) val links: Links? = null,
        @JsonDeserialize(using = ResourceLinkageDeserializer::class) val data: ResourceLinkage? = null,
        val meta: Meta? = null)

@JsonDeserialize(using = ValueDeserializer::class)
sealed class ValueMixin

@JsonDeserialize(using = JsObjectDeserializer::class)
sealed class JsObjectValueMixin

sealed class ErrorMixin(
        val id: String? = null,
        @JsonDeserialize(using = LinksDeserializer::class) val links: Links? = null,
        val status: String? = null,
        val code: String? = null,
        val title: String? = null,
        val detail: String? = null,
        val source: ErrorSource? = null,
        val meta: Meta? = null)

sealed class JsonApiInfoMixin

//
// Deserializers
//

class DataDeserializer : JsonDeserializer<Data>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Data {
        return when (parser.currentToken) {
            JsonToken.START_OBJECT -> parser.readValueAs(ResourceObject::class.java)
            JsonToken.START_ARRAY -> ResourceObjects(handleArray(parser))
            else -> context.reportInputMismatch(Data::class.java, "Unexpected token: ${parser.currentToken}")
        }
    }
}

class ResourceLinkageDeserializer : JsonDeserializer<ResourceLinkage>() {
    override fun getNullValue(ctxt: DeserializationContext?): ResourceLinkage {
        return EmptyToOneRelationship
    }

    override fun deserialize(parser: JsonParser, context: DeserializationContext): ResourceLinkage {
        return when (parser.currentToken) {
            JsonToken.START_OBJECT -> ToOneRelationship(parser.readValueAs(ResourceIdentifier::class.java))
            JsonToken.START_ARRAY -> ToManyRelationship(handleArray(parser))
            else -> context.reportInputMismatch(Data::class.java, "Unexpected token: ${parser.currentToken}")
        }
    }
}

class JsObjectDeserializer : JsonDeserializer<JsObjectValue>() {
    private val entryType = jacksonTypeRef<Map<String, Value>>()

    override fun deserialize(parser: JsonParser, context: DeserializationContext) =
            JsObjectValue(parser.readValueAs<Map<String, Value>>(entryType)
                    .map { (key, value) -> Attribute(key, value) })
}

class ValueDeserializer : JsonDeserializer<Value>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Value {
        return when (parser.currentToken) {
            JsonToken.VALUE_STRING -> StringValue(parser.text)
            JsonToken.VALUE_NUMBER_INT -> NumberValue(parser.decimalValue)
            JsonToken.VALUE_NUMBER_FLOAT -> NumberValue(parser.decimalValue)
            JsonToken.VALUE_TRUE -> TrueValue
            JsonToken.VALUE_FALSE -> FalseValue
            JsonToken.VALUE_NULL -> NullValue
            JsonToken.START_OBJECT -> parser.readValueAs(JsObjectValue::class.java)
            JsonToken.START_ARRAY -> JsArrayValue(handleArray(parser))
            else -> context.reportInputMismatch(Value::class.java, "Unexpected token: ${parser.currentToken}")
        }
    }

    override fun getNullValue(ctxt: DeserializationContext?): Value = NullValue
}

class RelationshipsDeserializer : JsonDeserializer<Relationships>() {
    private val relationshipsType = object : TypeReference<Relationships>() {}

    override fun getNullValue(ctxt: DeserializationContext) = emptyMap<String, Relationship>()

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Relationships {
        return parser.readValueAs(relationshipsType)
    }

}

class LinksDeserializer : JsonDeserializer<Links>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Links {
        val links = parser.readValueAs(JsObjectValue::class.java)
        return links.value.map {
            when (it.value) {
                is StringValue -> Link(it.name, it.value.value, meta = null)
                is JsObjectValue -> {
                    val attributes = it.value.value
                    val href = getAttribute<StringValue>(attributes, context, "href")
                            ?: context.reportInputMismatch(Link::class.java, "'href' attribute not found: ${it.value}")
                    val meta: Meta? = when (val metaObj = getAttribute<Value>(attributes, context, "meta")) {
                        is NullValue -> null
                        is JsObjectValue -> metaObj.value.fold(linkedMapOf(), { result, value ->
                            result[value.name] = value.value
                            return@fold result
                        })
                        else -> context.reportInputMismatch(Link::class.java, "Unexpected object: $it")
                    }

                    Link(it.name, href.value, meta)
                }
                else -> context.reportInputMismatch(Link::class.java, "Unexpected object: ${it.value}")
            }

        }
    }
}

class JsonApiInfoDeserializer : JsonDeserializer<JsonApiInfo>() {
    private val entryType = jacksonTypeRef<Map<String, Value>>()

    override fun deserialize(parser: JsonParser, context: DeserializationContext) =
            parser.readValueAs<Map<String, Value>>(entryType)
                    .map { (key, value) -> JsonApiEntry(key, value) }
}

private inline fun <reified T> getAttribute(attributes: Attributes, context: DeserializationContext, name: String): T? {
    return attributes.find { name == it.name }?.let {
        when (it.value) {
            is T -> it.value
            else -> context.reportInputMismatch(Link::class.java, "Unexpected object: ${it.value}")
        }
    }
}

private inline fun <reified T> handleArray(parser: JsonParser): List<T> {
    val next = parser.nextToken()
    return if (next != JsonToken.END_ARRAY)
        parser.readValuesAs(T::class.java).asSequence().toList()
    else {
        emptyList()
    }
}