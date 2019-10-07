package com.systemfreund.jsonapi.test.hamcrest

import com.systemfreund.jsonapi.ResourceIdentifier
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher

object ResourceIdentifierMatchers {
    @JvmStatic
    fun idEqualTo(id: String): Matcher<ResourceIdentifier> {
        return withId(equalTo(id))
    }

    @JvmStatic
    fun withId(idMatcher: Matcher<String>): Matcher<ResourceIdentifier> {
        return object : FeatureMatcher<ResourceIdentifier, String>(idMatcher, "id", "id") {
            override fun featureValueOf(actual: ResourceIdentifier): String {
                return actual.id
            }
        }
    }

    @JvmStatic
    fun typeEqualTo(type: String): Matcher<ResourceIdentifier> {
        return withType(equalTo(type))
    }

    @JvmStatic
    fun withType(typeMatcher: Matcher<String>): Matcher<ResourceIdentifier> {
        return object : FeatureMatcher<ResourceIdentifier, String>(typeMatcher, "type($typeMatcher)", "type") {
            override fun featureValueOf(actual: ResourceIdentifier): String {
                return actual.type
            }
        }
    }

    // TODO matchers for meta field
}