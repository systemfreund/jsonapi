package com.systemfreund.jsonapi

interface DocumentWriter<T> {
    fun toDocument(t: T): Document
}
