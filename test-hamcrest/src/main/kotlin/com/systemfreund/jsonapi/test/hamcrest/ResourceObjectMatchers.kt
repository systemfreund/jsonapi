package com.systemfreund.jsonapi.test.hamcrest

import com.systemfreund.jsonapi.Document.Companion.ResourceObject
import com.systemfreund.jsonapi.Relationship
import com.systemfreund.jsonapi.ResourceIdentifier
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher
import java.lang.String.format

object ResourceObjectMatchers {
    /**
     * Verify that a resource object's resource identifier field match the given matcher.
     */
    @JvmStatic
    fun hasResourceId(resourceIdMatcher: Matcher<ResourceIdentifier>): Matcher<ResourceObject> {
        return object : FeatureMatcher<ResourceObject, ResourceIdentifier>(resourceIdMatcher, ".resourceId=", "resourceId") {
            override fun featureValueOf(actual: ResourceObject): ResourceIdentifier? {
                return actual.resourceIdentifier
            }
        }
    }

    /**
     * Verify that a resource object's resource identifier is equal to the given prototype.
     */
    @JvmStatic
    fun resourceWithId(resourceIdMatcher: ResourceIdentifier): Matcher<ResourceObject> {
        return hasResourceId(equalTo(resourceIdMatcher))
    }

    /**
     * Verify that a resource object's resource identifier is equal to the given values.
     */
    @JvmStatic
    fun resourceWithId(id: String, type: String): Matcher<ResourceObject> {
        return resourceWithId(ResourceIdentifier(id, type, null))
    }

    /**
     * Match a relationship in a resource object.
     */
    @JvmStatic
    fun hasRelationship(rel: String, relMatcher: Matcher<Relationship>): Matcher<ResourceObject> {
        val featureName = format("relationship['%s']", rel)
        return object : FeatureMatcher<ResourceObject, Relationship>(relMatcher, featureName, featureName) {
            override fun featureValueOf(actual: ResourceObject): Relationship? {
                return actual.relationships[rel]
            }
        }
    }

    @JvmStatic
    fun linkageExists(rel: String): Matcher<ResourceObject> {
        return hasRelationship(rel, RelationshipMatchers.linkageExists())
    }
}