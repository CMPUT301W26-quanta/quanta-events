package ca.quanta.quantaevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.allOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ca.quanta.quantaevents.fragments.HomeFragment;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SmartBurgerNavigationTest {



    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new
            ActivityScenarioRule<>(MainActivity.class);

    private void openBurgerMenu(){
        onView(allOf(withContentDescription("Open Menu"), isDescendantOfA(withId(R.id.coordinator)))).perform(click());
    }



    /**
     * Home button icon should not be part of burger menu when navigated ot home
     * Home icon should not appear in burger menu
     */

    @Test
    public void testBurgerMenuHidesCurrentPage_Home(){

        openBurgerMenu();
        onView(allOf(withContentDescription("Home"), isDescendantOfA(withId(R.id.coordinator)))).perform(click());

        openBurgerMenu();

        onView(withContentDescription("Home")).check(doesNotExist());


    }


    @Test
    public void testBurgerMenuHidesCurrentPage_AdminPanel(){

        openBurgerMenu();
        onView(allOf(withContentDescription("Admin Panel"), isDescendantOfA(withId(R.id.coordinator)))).perform(click());

        openBurgerMenu();

        onView(withContentDescription("Admin Panel")).check(doesNotExist());
    }

    @Test
    public void testBurgerMenuHidesCurrentPage_Dashboard(){

        openBurgerMenu();
        onView(allOf(withContentDescription("Dashboard"), isDescendantOfA(withId(R.id.coordinator)))).perform(click());

        openBurgerMenu();

        onView(withContentDescription("Dashboard")).check(doesNotExist());
    }
    @Test
    public void testBurgerMenuHidesCurrentPage_EventList(){

        openBurgerMenu();
        onView(allOf(withContentDescription("Event List"), isDescendantOfA(withId(R.id.coordinator)))).perform(click());

        openBurgerMenu();

        onView(withContentDescription("Event List")).check(doesNotExist());
    }

    @Test
    public void testBurgerMenuHidesCurrentPage_Information(){

        openBurgerMenu();

        onView(allOf(withContentDescription("Home"), isDescendantOfA(withId(R.id.coordinator)))).perform(click());

        openBurgerMenu();

        onView(allOf(withContentDescription("Information"), isDescendantOfA(withId(R.id.coordinator)))).perform(click());

        openBurgerMenu();

        onView(withContentDescription("Information")).check(doesNotExist());
    }


}
