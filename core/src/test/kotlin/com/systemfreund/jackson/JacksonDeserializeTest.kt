package com.systemfreund.jackson

import com.fasterxml.jackson.module.kotlin.readValue
import com.systemfreund.*
import com.systemfreund.Document.Companion.ResourceObject
import com.systemfreund.Document.Companion.ResourceObjects
import com.systemfreund.JsonApiObject.FalseValue
import com.systemfreund.JsonApiObject.JsArrayValue
import com.systemfreund.JsonApiObject.JsObjectValue
import com.systemfreund.JsonApiObject.NullValue
import com.systemfreund.JsonApiObject.NumberValue
import com.systemfreund.JsonApiObject.StringValue
import com.systemfreund.JsonApiObject.TrueValue
import org.junit.Test
import java.math.BigDecimal

class JacksonDeserializeTest {

    val mapper = JsonApiJackson.createMapper()

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
                        "k1" to JsonApiObject.StringValue("v1"),
                        "k2" to JsonApiObject.NumberValue(BigDecimal.valueOf(1234))
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
                data = ResourceObject(id = "1234", type = "model", links = emptyList())))

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
                data = ResourceObject(id = "1234", type = "model", links = listOf(
                        Link.self("http://localhost")
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
                                "k1" to JsonApiObject.StringValue("v1"),
                                "k2" to JsonApiObject.NumberValue(BigDecimal.valueOf(1234))
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
                        links = listOf(Link.self("http://localhost")))
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
                        links = listOf())
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
        """.assertEquals(Document(links = emptyList()))

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
        """.assertEquals(Document(links = listOf(
                Link.self("http://localhost"),
                Link.next("http://localhost/next"),
                Link.prev("http://localhost/prev", meta = emptyMap()),
                Link.about("http://localhost/about", meta = mapOf(
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
        """.assertEquals(Document(ResourceObject(type = "model", relationships = null)))

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
              "rel1": {
                "links": {
                  "self": "http://localhost/rel1"
                },
                "data": {
                  "id" : "1234",
                  "type" : "model"
                }
              },
              "rel2": {
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
              "rel3": {
                "data": null
              },
              "rel4": {
                "data": []
              }
            }
          }
        }
        """.assertEquals(Document(ResourceObject(type = "model", relationships = mapOf(
                "rel1" to Relationship(
                        links = listOf(Link.self("http://localhost/rel1")),
                        data = ResourceObject("1234", "model")),
                "rel2" to Relationship(
                        data = ResourceObjects(listOf(
                                ResourceObject("1", "model"),
                                ResourceObject("2", "model"))),
                        meta = mapOf("m1" to FalseValue)),
                "rel3" to Relationship(data = null),
                "rel4" to Relationship(data = ResourceObjects(emptyList()))
        ))))
    }
}