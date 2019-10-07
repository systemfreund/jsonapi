package com.systemfreund.jsonapi.test.hamcrest

import com.systemfreund.jsonapi.ResourceIdentifier
import com.systemfreund.jsonapi.ResourceLinkage.ToOneRelationship
import org.hamcrest.FeatureMatcher
import org.hamcrest.Matcher

object ResourceLinkageMatchers {
    @JvmStatic
    fun hasResourceId(idMatcher: Matcher<in ResourceIdentifier>): Matcher<ToOneRelationship> {
        return object : FeatureMatcher<ToOneRelationship, ResourceIdentifier>(idMatcher, "id($idMatcher)", "id") {
            override fun featureValueOf(actual: ToOneRelationship): ResourceIdentifier? {
                return actual.id
            }
        }
    }
}