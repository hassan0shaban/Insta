package com.whiteside.insta

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestExample {

    @Rule
    @JvmField
    val rule : ActivityTestRule<WallActivity> = ActivityTestRule(WallActivity::class.java)

    @Test
    fun testAddImage() {
        onView(withId(R.id.add_image)).perform(click())
    }

}