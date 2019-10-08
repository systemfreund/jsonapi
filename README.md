# Untitled JSON API Test Library

Work in progress library to make testing of JSON-API based APIs easier.

## Hamcrest Example:

```kotlin
val response: Document = GET("/api/users/1")

assertThat(response, allOf(
   // Make sure the primary data object is not a collection resource, 
   // but contains a singular resource that is identified by the given parameters.
   isSingularResource(id = "U1", type = "users"),
   
   // Make sure `picture` relationship's linkage[1] object
   // is a to-one relationship that is identified by the given parameters.
   hasToOneRelationshipWithId(relation = "picture", "P1"),
   
   hasAttributes(
       attribute("given-name", equalTo("Lester")),
       attribute("family-name", equalTo("Nygaard"))
))
```

