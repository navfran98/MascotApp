package com.barkindustries.mascotapp;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;


/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SignUpInstrumentedTest {

    @Rule
    ActivityTestRule<SignUpActivity> activityTestRule =
            new ActivityTestRule<>(SignUpActivity.class);

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.barkindustries.mascotapp", appContext.getPackageName());
    }

    @Test
    public void checkSignUpSuccess() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.email_input_from_signup)).perform(typeText("example@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.username_input_from_signup)).perform(typeText("example"), closeSoftKeyboard());
        onView(withId(R.id.password_input_from_signup)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.password_confirm_input_from_signup)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.signup_button_from_signup)).check(matches(isDisplayed())).perform(click());
        onView(withText("Sign Up successful.")).check(matches(isDisplayed()));

    }

    @Test
    public void checkSignUpPasswordsNotMatch() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.email_input_from_signup)).perform(typeText("example@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.username_input_from_signup)).perform(typeText("example"), closeSoftKeyboard());
        onView(withId(R.id.password_input_from_signup)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.password_confirm_input_from_signup)).perform(typeText("1234"), closeSoftKeyboard());
        onView(withId(R.id.signup_button_from_signup)).check(matches(isDisplayed())).perform(click());
        onView(withText("Sign Up not successful. Passwords do not match")).check(matches(isDisplayed()));

    }

}
