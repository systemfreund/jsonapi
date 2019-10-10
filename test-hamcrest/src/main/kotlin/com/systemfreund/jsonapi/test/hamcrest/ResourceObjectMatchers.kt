package com.systemfreund.jsonapi.test.hamcrest

import com.systemfreund.jsonapi.Document.Companion.ResourceObject
import com.systemfreund.jsonapi.Relationship
import com.systemfreund.jsonapi.ResourceIdentifier
import com.systemfreund.jsonapi.test.hamcrest.RelationshipMatchers.isToOneRelationship
import com.systemfreund.jsonapi.test.hamcrest.RelationshipMatchers.linkageExists
import com.systemfreund.jsonapi.test.hamcrest.ResourceIdentifierMatchers.withId
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher

object ResourceObjectMatchers {
    /**
     * Verify that a to-one relationship has a linkage object that points to a resource with the specified id.
     */
    @JvmStatic
    fun hasToOneRelationshipWithId(relationName: String, id: String) = hasToOneRelationshipWithId(relationName, equalTo(id))

    /**
     * Verify that a to-one relationship has a linkage object that points to a resource whose id matches.
     */
    @JvmStatic
    fun hasToOneRelationshipWithId(relationName: String, id: Matcher<String>) =
            hasRelationship(relationName, isToOneRelationship(ResourceLinkageMatchers.hasResourceId(withId(id))))

    /**
     * Verify that a relationship is present, but has no linkage, i.e. `data` is absent from the resource object.
     */
    @JvmStatic
    fun hasRelationshipWithoutLinkage(name: String) = hasRelationship(name, not(linkageExists()))

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
    fun resourceWithId(resourceIdMatcher: ResourceIdentifier) = hasResourceId(equalTo(resourceIdMatcher))

    /**
     * Verify that a resource object's resource identifier is equal to the given values.
     */
    @JvmStatic
    fun resourceWithId(id: String, type: String) = resourceWithId(ResourceIdentifier(id, type, null))

    /**
     * Match a relationship in a resource object.
     */
    @JvmStatic
    fun hasRelationship(rel: String, relMatcher: Matcher<Relationship>): Matcher<ResourceObject> {
        return object : TypeSafeDiagnosingMatcher<ResourceObject>() {
            override fun matchesSafely(resourceObject: ResourceObject, mismatchDescription: Description): Boolean {
                val relationship = resourceObject.relationships[rel]
                if (relationship == null) {
                    mismatchDescription.appendText("relationship '$rel' not found")
                    return false
                }

                if (!relMatcher.matches(relationship)) {
                    relMatcher.describeMismatch(relationship, mismatchDescription)
                    return false
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("relationship '$rel' ")
                relMatcher.describeTo(description)
            }

        }
    }

    @JvmStatic
    fun linkageExists(rel: String): Matcher<ResourceObject> {
        return hasRelationship(rel, linkageExists())
    }
}