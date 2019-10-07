package com.systemfreund.jsonapi.jackson

import com.fasterxml.jackson.module.kotlin.readValue
import com.systemfreund.jsonapi.Attribute
import com.systemfreund.jsonapi.Document
import com.systemfreund.jsonapi.Document.Companion.ResourceObject
import com.systemfreund.jsonapi.Document.Companion.ResourceObjects
import com.systemfreund.jsonapi.Error
import com.systemfreund.jsonapi.ErrorSource
import com.systemfreund.jsonapi.JsonApiEntry
import com.systemfreund.jsonapi.JsonApiJackson
import com.systemfreund.jsonapi.JsonApiObject.FalseValue
import com.systemfreund.jsonapi.JsonApiObject.JsArrayValue
import com.systemfreund.jsonapi.JsonApiObject.JsObjectValue
import com.systemfreund.jsonapi.JsonApiObject.NullValue
import com.systemfreund.jsonapi.JsonApiObject.NumberValue
import com.systemfreund.jsonapi.JsonApiObject.StringValue
import com.systemfreund.jsonapi.JsonApiObject.TrueValue
import com.systemfreund.jsonapi.Link
import com.systemfreund.jsonapi.Relationship
import com.systemfreund.jsonapi.ResourceIdentifier
import com.systemfreund.jsonapi.ResourceLinkage.ToManyRelationship
import com.systemfreund.jsonapi.ResourceLinkage.ToOneRelationship
import org.junit.Test
import java.math.BigDecimal

class JacksonDeserializeTest {

    private val mapper = JsonApiJackson.createMapper()

    private inline fun <reified T : Any> String.assertEquals(expected: T) = kotlin.test.assertEquals(expected, mapper.readValue(this))

    @Test
    fun `primary data is absent`() {
        """
        {
        }
        """.assertEquals(Document())

        """
        {
          "data": null
        }
        """.assertEquals(Document())
    }

    @Test
    fun `primary data is an object`() {
        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model"
          }
        }
        """.assertEquals(Document(ResourceObject("1234", "model")))
    }

    @Test
    fun `primary data is an array`() {
        """
        {
          "data": [
            {
              "id" : "1",
              "type" : "model"
            },
            {
              "id" : "2",
              "type" : "model"
            }
          ]
        }
        """.assertEquals(Document(ResourceObjects(listOf(
                ResourceObject("1", "model"),
                ResourceObject("2", "model")))))

        """
        {
          "data": [ ]
        }
        """.assertEquals(Document(ResourceObjects(listOf())))
    }

    @Test
    fun `deserialize primitive values`() {
        """
        {
          "string": "hello world",
          "integer": 1234567890,
          "float": 1234.56789,
          "true": true,
          "false": false,
          "null": null
        }
        """.assertEquals(mapOf(
                "string" to StringValue("hello world"),
                "integer" to NumberValue(BigDecimal.valueOf(1234567890)),
                "float" to NumberValue(BigDecimal.valueOf(1234.56789)),
                "true" to TrueValue,
                "false" to FalseValue,
                "null" to NullValue))
    }

    @Test
    fun `deserialize object values`() {
        """
        {
          "a1": "v1",
          "a2": 2,
          "a3": null,
          "a4": {
            "a41": "v41",
            "a42": false
          },
          "a5": ["a", "b"]
        }
        """.assertEquals(JsObjectValue(listOf(
                Attribute("a1", StringValue("v1")),
                Attribute("a2", NumberValue(BigDecimal.valueOf(2))),
                Attribute("a3", NullValue),
                Attribute("a4", JsObjectValue(listOf(
                        Attribute("a41", StringValue("v41")),
                        Attribute("a42", FalseValue)
                ))),
                Attribute("a5", JsArrayValue(listOf(
                        StringValue("a"),
                        StringValue("b")))))))
    }

    @Test
    fun `deserialize empty arrays`() {
        """
        []
        """.assertEquals(JsArrayValue(emptyList()))

        """
        [ [ [] ] ]
        """.assertEquals(
                JsArrayValue(listOf(
                        JsArrayValue(listOf(
                                JsArrayValue(emptyList()))))))
    }

    @Test
    fun `deserialize array values`() {
        """
        [
          "v1",
          2,
          null,
          [],
          [ "a", [ "b", [], {} ] ],
          { "a1": "v1", "a2": true }
        ]
        """.assertEquals(JsArrayValue(listOf(
                StringValue("v1"),
                NumberValue(BigDecimal.valueOf(2)),
                NullValue,
                JsArrayValue(emptyList()),
                JsArrayValue(listOf(
                        StringValue("a"),
                        JsArrayValue(listOf(
                                StringValue("b"),
                                JsArrayValue(emptyList()),
                                JsObjectValue(emptyList()))))),
                JsObjectValue(listOf(
                        Attribute("a1", StringValue("v1")),
                        Attribute("a2", TrueValue))))))
    }

    @Test
    fun `deserialize meta object in document`() {
        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model"
          },
          "meta": {
            "k1": "v1",
            "k2": 1234
          }
        }
        """.assertEquals(Document(
                data = ResourceObject("1234", "model"),
                meta = mapOf(
                        "k1" to StringValue("v1"),
                        "k2" to NumberValue(BigDecimal.valueOf(1234))
                )))
    }

    @Test
    fun `data contains attributes`() {
        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model",
            "attributes": { }
          }
        }
        """.assertEquals(Document(
                data = ResourceObject("1234", "model", JsObjectValue(emptyList()))))

        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model",
            "attributes": null
          }
        }
        """.assertEquals(Document(
                data = ResourceObject("1234", "model", null)))

        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model",
            "attributes": {
               "a1": "v1",
               "a2": true
            }
          }
        }
        """.assertEquals(Document(
                data = ResourceObject("1234", "model", JsObjectValue(listOf(
                        Attribute("a1", StringValue("v1")),
                        Attribute("a2", TrueValue)
                )))))
    }

    @Test
    fun `deserialize links object in primary data`() {
        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model",
            "links": null
          }
        }
        """.assertEquals(Document(
                data = ResourceObject(id = "1234", type = "model", links = null)))

        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model",
            "links": { }
          }
        }
        """.assertEquals(Document(
                data = ResourceObject(id = "1234", type = "model", links = emptyMap())))

        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model",
            "links": {
              "self": "http://localhost"
            }
          }
        }
        """.assertEquals(Document(
                data = ResourceObject(id = "1234", type = "model", links = mapOf(
                        "self" to Link("http://localhost")
                ))))
    }

    @Test
    fun `deserialize meta object in primary data`() {
        """
        {
          "data" : {
            "id" : "1234",
            "type" : "model",
            "meta": {
              "k1": "v1",
              "k2": 1234
            }
          }
        }
        """.assertEquals(Document(
                data = ResourceObject("1234", "model",
                        meta = mapOf(
                                "k1" to StringValue("v1"),
                                "k2" to NumberValue(BigDecimal.valueOf(1234))
                        ))))
    }

    @Test
    fun `deserialize error object`() {
        """
        {
          "errors" : null
        }
        """.assertEquals(Document(errors = null))

        """
        {
          "errors" : []
        }
        """.assertEquals(Document(errors = emptyList()))

        """
        {
          "errors" : [
            {
              "id": "error1",
              "status": "500",
              "code": "ERR1",
              "title": "Error 1",
              "detail": "Details",
              "source": {
                "pointer": "/data/attributes/field",
                "parameter": "query"
              },
              "meta": {
                "key": "value"
              },
              "links": {
                "self": "http://localhost"
              }
            }
          ]
        }
        """.assertEquals(Document(errors = listOf(
                Error(id = "error1",
                        status = "500",
                        code = "ERR1",
                        title = "Error 1",
                        detail = "Details",
                        source = ErrorSource("/data/attributes/field", "query"),
                        meta = mapOf("key" to StringValue("value")),
                        links = mapOf("self" to Link("http://localhost")))
        )))

        """
        {
          "errors" : [
            {
              "source": { },
              "meta": { },
              "links": { }
            }
          ]
        }
        """.assertEquals(Document(errors = listOf(
                Error(source = ErrorSource(),
                        meta = emptyMap(),
                        links = emptyMap())
        )))
    }

    @Test
    fun `deserialize links object in document`() {
        """
        {
          "links": null
        }
        """.assertEquals(Document(links = null))

        """
        {
          "links": { }
        }
        """.assertEquals(Document(links = emptyMap()))

        """
        {
          "links": {
            "self": "http://localhost",
            "next": {
              "href": "http://localhost/next",
              "meta": null
            },
            "prev": {
              "href": "http://localhost/prev",
              "meta": {}
            },
            "about": {
              "href": "http://localhost/about",
              "meta": {
                "author": "the author",
                "version": 1
              }
            }
          }
        }
        """.assertEquals(Document(links = mapOf(
                "self" to Link("http://localhost"),
                "next" to Link("http://localhost/next"),
                "prev" to Link("http://localhost/prev", meta = emptyMap()),
                "about" to Link("http://localhost/about", meta = mapOf(
                        "author" to StringValue("the author"),
                        "version" to NumberValue(BigDecimal.valueOf(1))
                ))
        )))
    }

    @Test
    fun `deserialize included objects in document`() {
        """
        {
          "included": [
            {
              "id" : "1",
              "type" : "model",
              "attributes": {
                 "a1": "v1"
              }
            },
            {
              "id" : "2",
              "type" : "model"
            }
          ]
        }
        """.assertEquals(Document(included = ResourceObjects(listOf(
                ResourceObject("1", "model", JsObjectValue(listOf(Attribute("a1", StringValue("v1"))))),
                ResourceObject("2", "model")
        ))))
    }

    @Test
    fun `deserialize jsonapi info in document`() {
        """
        {
          "jsonapi": null
        }
        """.assertEquals(Document(jsonapi = null))

        """
        {
          "jsonapi": {}
        }
        """.assertEquals(Document(jsonapi = emptyList()))

        """
        {
          "jsonapi": {
            "version": "1.0",
            "meta": {
              "m1": "v1"
            }
          }
        }
        """.assertEquals(Document(jsonapi = listOf(
                JsonApiEntry("version", StringValue("1.0")),
                JsonApiEntry("meta", JsObjectValue(listOf(
                        Attribute("m1", StringValue("v1"))
                )))
        )))
    }

    @Test
    fun `deserialize relationships object in primary data`() {
        """
        {
          "data": {
            "type": "model",
            "relationships": null
          }
        }
        """.assertEquals(Document(ResourceObject(type = "model", relationships = emptyMap())))

        """
        {
          "data": {
            "type": "model",
            "relationships": { }
          }
        }
        """.assertEquals(Document(ResourceObject(type = "model", relationships = emptyMap())))

        """
        {
          "data": {
            "type": "model",
            "relationships": {
              "with-to-one-linkage": {
                "links": {
                  "self": "http://localhost/rel1"
                },
                "data": {
                  "id" : "1234",
                  "type" : "model"
                }
              },
              "with-to-many-linkage": {
                "data": [
                  {
                    "id" : "1",
                    "type" : "model"
                  },
                  {
                    "id" : "2",
                    "type" : "model"
                  }
                ],
                "meta": {
                  "m1": false
                }
              },
              "with-empty-to-one-linkage": {
                "data": null
              },
              "with-empty-to-many-linkage": {
                "data": []
              },
              "with-absent-linkage": {
              }
            }
          }
        }
        """.assertEquals(Document(ResourceObject(type = "model", relationships = mapOf(
                "with-to-one-linkage" to Relationship(
                        links = mapOf("self" to Link("http://localhost/rel1")),
                        data = ToOneRelationship(ResourceIdentifier("1234", "model"))),
                "with-to-many-linkage" to Relationship(
                        data = ToManyRelationship(listOf(
                                ResourceIdentifier("1", "model"),
                                ResourceIdentifier("2", "model"))),
                        meta = mapOf("m1" to FalseValue)),
                "with-empty-to-one-linkage" to Relationship(data = ToOneRelationship()),
                "with-empty-to-many-linkage" to Relationship(data = ToManyRelationship()),
                "with-absent-linkage" to Relationship(data = null)
        ))))
    }
}