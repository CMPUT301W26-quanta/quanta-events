package ca.quanta.quantaevents;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)

public class CommentsUITesting {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void navigateToEventDetails(){

        Matcher<View> burgerButton = allOf(withContentDescription("Open Menu"), isDescendantOfA(withId(R.id.coordinator)));

        onView(burgerButton).perform(click());
        clickMenuItem("Event List");

        onView(withId(R.id.events_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));


    }

    @Test
    public void commentInputIsDisplayed(){
        onView(withId(R.id.comment_text)).check(matches(isDisplayed()));

    }

    @Test
    public void typingCommentShowsTextInInput(){
        onView(withId(R.id.comment_text)).perform(typeText("This is a test comment"), closeSoftKeyboard());

        onView(withId(R.id.comment_text)).check(matches(withText("This is a test comment")));

    }

    @Test
    public void postingCommentAppearsInList(){
        String testMessage = "Hello this is a test comment";

        onView(withId(R.id.comment_text)).perform(typeText(testMessage), closeSoftKeyboard());

        onView(withId(R.id.send_comment)).perform(click());

        onView(withId(R.id.comments_recycler_view)).check(matches(hasDescendant(withText(testMessage))));

    }

    @Test
    public void sendButtonIsDisplayed(){
        onView(withId(R.id.send_comment)).check(matches(isDisplayed()));

    }

    @Test
    public void emptyCommentDoesNotPost(){
        onView(withId(R.id.send_comment)).perform(click());

        onView(withId(R.id.comment_text)).check(matches(withText("")));
    }


    private void clickMenuItem(String label){
        Matcher<View> item = allOf(withContentDescription(label), isDescendantOfA(withId(R.id.coordinator)));

        onView(item).perform(click());
    }


}
