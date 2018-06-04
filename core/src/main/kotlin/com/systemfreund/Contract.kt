package com.systemfreund

interface DocumentWriter<T> {
    fun toDocument(t: T): Document
}
