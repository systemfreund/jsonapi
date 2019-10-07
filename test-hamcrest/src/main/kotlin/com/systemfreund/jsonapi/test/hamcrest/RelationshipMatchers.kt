package com.systemfreund.jsonapi.test.hamcrest

import com.systemfreund.jsonapi.Relationship
import com.systemfreund.jsonapi.ResourceLinkage

import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers

import com.systemfreund.jsonapi.ResourceLinkage.ToOneRelationship
import java.lang.String.format
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.TypeSafeDiagnosingMatcher

object RelationshipMatchers {
    /**
     * Run an arbitrary matcher on a relationship's linkage.
     */
    @JvmStatic
    fun hasLinkage(linkageMatcher: Matcher<out ResourceLinkage>): Matcher<Relationship> {
        return object : TypeSafeDiagnosingMatcher<Relationship>() {
            override fun matchesSafely(item: Relationship, mismatchDescription: Description): Boolean {
                val actualLinkage = item.data

                if (!linkageMatcher.matches(actualLinkage)) {
                    mismatchDescription.appendText("data ")
                    linkageMatcher.describeMismatch(actualLinkage, mismatchDescription)
                    return false
                }

                return true
            }

            override fun describeTo(description: Description) {
                description.appendText(format(".data == %s", linkageMatcher))
            }
        }
    }

    /**
     * Verify that a linkage object is present, i.e. `data` attribute.
     *
     * @see [Relationships](https://jsonapi.org/format/.document-resource-object-relationships)
     */
    @JvmStatic
    fun linkageExists(): Matcher<Relationship> {
        return hasLinkage(Matchers.notNullValue(ResourceLinkage::class.java))
    }

    @JvmStatic
    fun isToOneRelationship(linkageMatcher: Matcher<ToOneRelationship>): Matcher<Relationship> {
        return hasLinkage(allOf(Matchers.instanceOf<Any>(ToOneRelationship::class.java), linkageMatcher))
    }

    // TODO links and meta matchers
}