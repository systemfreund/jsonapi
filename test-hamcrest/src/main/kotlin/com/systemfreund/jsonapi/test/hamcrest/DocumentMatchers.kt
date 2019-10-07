package com.systemfreund.jsonapi.test.hamcrest

import com.systemfreund.jsonapi.Document
import com.systemfreund.jsonapi.Document.Companion.ResourceObject
import com.systemfreund.jsonapi.Document.Companion.ResourceObjects
import com.systemfreund.jsonapi.Relationship
import com.systemfreund.jsonapi.ResourceLinkage.ToOneRelationship
import com.systemfreund.jsonapi.test.hamcrest.RelationshipMatchers.isToOneRelationship
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.everyItem
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.iterableWithSize
import org.hamcrest.TypeSafeDiagnosingMatcher

object DocumentMatchers {
    /**
     * Verify that a singular resource's id matches.
     */
    @JvmStatic
    fun isSingularResource(id: String, type: String): Matcher<Document> {
        return isSingularResource(ResourceObjectMatchers.resourceWithId(id, type))
    }

    @JvmStatic
    fun isSingularResource(primaryDataMatcher: Matcher<ResourceObject>): Matcher<Document> {
        return object : TypeSafeDiagnosingMatcher<Document>() {
            override fun matchesSafely(item: Document, mismatchDescription: Description): Boolean {
                if (item.data !is ResourceObject) {
                    mismatchDescription.appendText("primary data is not singular")
                    return false
                }

                if (!primaryDataMatcher.matches(item.data)) {
                    primaryDataMatcher.describeMismatch(item.data, mismatchDescription)
                    return false
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("document.data")
                primaryDataMatcher.describeTo(description)
            }
        }
    }

    @JvmStatic
    fun isCollectionResourceOfSize(number: Int): Matcher<Document> {
        return isCollectionResource(iterableWithSize(number))
    }

    @JvmStatic
    fun <R : Iterable<ResourceObject>> isCollectionResource(primaryDataMatcher: Matcher<R>): Matcher<Document> {
        return object : TypeSafeDiagnosingMatcher<Document>() {
            override fun matchesSafely(item: Document, mismatchDescription: Description): Boolean {
                if (item.data !is ResourceObjects) {
                    mismatchDescription.appendText("primary data is not a collection")
                    return false
                }

                if (!primaryDataMatcher.matches(item.data)) {
                    primaryDataMatcher.describeMismatch(item.data, mismatchDescription)
                    return false
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("document.data ")
                primaryDataMatcher.describeTo(description)
            }
        }
    }

    /**
     * Verify that a to-one relationship is empty, i.e. `data==null`.
     *
     *
     * Note: this matcher returns `false` if the `data` attribute (linkage) is missing.
     * An absent `data` means we don't know what the relationship is exactly.
     */
    @JvmStatic
    fun hasEmptyToOneRelationship(name: String): Matcher<Document> {
        return hasToOneRelationship(name, equalTo(ToOneRelationship(null)))
    }

    /**
     * Verify that a relationship is present, but has no linkage, i.e. `data` is absent from the document.
     */
    @JvmStatic
    fun hasRelationshipWithoutLinkage(name: String): Matcher<Document> {
        return hasRelationship(name, not(RelationshipMatchers.linkageExists()))
    }

    /**
     * Verify that a to-one relationship has a linkage object that points to a resource with the specified id.
     */
    @JvmStatic
    fun hasToOneRelationshipWithId(relationName: String, id: String): Matcher<Document> {
        return hasToOneRelationshipWithId(relationName, equalTo(id))
    }

    /**
     * Verify that a to-one relationship has a linkage object that points to a resource whose id matches.
     */
    @JvmStatic
    fun hasToOneRelationshipWithId(relationName: String, id: Matcher<String>): Matcher<Document> {
        return hasToOneRelationship(relationName, ResourceLinkageMatchers.hasResourceId(ResourceIdentifierMatchers.withId(id)))
    }

    /**
     * Verify that a to-one relationship has a linkage object that matches.
     */
    @JvmStatic
    fun hasToOneRelationship(relationName: String, relMatcher: Matcher<ToOneRelationship>): Matcher<Document> {
        return hasRelationship(relationName, isToOneRelationship(relMatcher))
    }

    @JvmStatic
    fun hasRelationship(relationName: String, relMatcher: Matcher<Relationship>): Matcher<Document> {
        return isSingularResource(ResourceObjectMatchers.hasRelationship(relationName, relMatcher))
    }

    /**
     * Every included ResourceObject must match the specified matcher.
     */
    @JvmStatic
    fun everyIncluded(includedResourceMatcher: Matcher<ResourceObject>): Matcher<Document> {
        return object : TypeSafeDiagnosingMatcher<Document>() {
            override fun matchesSafely(item: Document, mismatchDescription: Description): Boolean {
                val included = item.included

                if (included == null) {
                    mismatchDescription.appendText("no included resources")
                    return false
                }

                val delegate = everyItem(includedResourceMatcher)
                if (!delegate.matches(included.array)) {
                    delegate.describeMismatch(included.array, mismatchDescription)
                    return false
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("every included resource is ").appendDescriptionOf(includedResourceMatcher)
            }
        }
    }

    @JvmStatic
    @SafeVarargs
    fun hasInclusions(vararg matchers: Matcher<ResourceObject>): Matcher<Document> {
        return inclusions(containsInAnyOrder(*matchers))
    }

    @JvmStatic
    fun <R : Iterable<ResourceObject>> inclusions(includedResourcesMatcher: Matcher<R>): Matcher<Document> {
        return object : TypeSafeDiagnosingMatcher<Document>() {
            override fun matchesSafely(item: Document, mismatchDescription: Description): Boolean {
                val resourceObjects = item.included
                if (resourceObjects == null) {
                    mismatchDescription.appendText("no included resources")
                    return false
                }

                if (!includedResourcesMatcher.matches(resourceObjects.array)) {
                    includedResourcesMatcher.describeMismatch(resourceObjects.array, mismatchDescription)
                    return false
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText("document.included ")
                includedResourcesMatcher.describeTo(description)
            }
        }
    }
}