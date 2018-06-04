package com.systemfreund

fun Document.Companion.ResourceObject.assertHasRelationships(expected: List<Pair<String, Relationship>>) {
    val expecctedMap = linkedMapOf(*expected.toTypedArray())

}