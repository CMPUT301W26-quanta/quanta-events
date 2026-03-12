package ca.quanta.quantaevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.allOf;

import android.view.View;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;


import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(AndroidJUnit4.class)
@LargeTest
public class SmartBurgerNavigationTest {

    @Before
    public void skipRegistration() {
        try {
            waitForView(withId(R.id.save_button));

            onView(withId(R.id.input_name)).perform(typeText("Test User"), closeSoftKeyboard());

            onView(withId(R.id.input_email)).perform(typeText("test@example.com"), closeSoftKeyboard());

            onView(withId(R.id.save_button)).perform(click());
        } catch (Throwable e) {

        }
    }



@Rule
    public ActivityScenarioRule<MainActivity> activityRule = new
            ActivityScenarioRule<>(MainActivity.class);


    private void waitForView(Matcher<View> matcher){

        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end){
            try {
                onView(matcher).check(matches(isDisplayed()));
                return;
            } catch (Throwable e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        }
        onView(matcher).check(matches(isDisplayed()));

    }

    private void openBurgerMenu(){
        Matcher<View> burgerButton = allOf(withContentDescription("Open Menu"), isDescendantOfA(withId(R.id.coordinator)));

        waitForView(burgerButton);
        onView(burgerButton).perform(click());
    }

    private void clickMenuItem(String label){
        Matcher<View> item = allOf(withContentDescription(label), isDescendantOfA(withId(R.id.coordinator)));

        waitForView(item);
        onView(item).perform(click());
    }



    /**
     * Home button icon should not be part of burger menu when navigated ot home
     * Home icon should not appear in burger menu
     */

    @Test
    public void testBurgerMenuHidesCurrentPage_Home(){

        openBurgerMenu();
        clickMenuItem("Home");
        openBurgerMenu();

        onView(withContentDescription("Home")).check(doesNotExist());


    }


    @Test
    public void testBurgerMenuHidesCurrentPage_AdminPanel(){

        openBurgerMenu();
        clickMenuItem("Admin Label");
        openBurgerMenu();

        onView(withContentDescription("Admin Panel")).check(doesNotExist());
    }

    @Test
    public void testBurgerMenuHidesCurrentPage_Dashboard(){

        openBurgerMenu();
        clickMenuItem("Dashboard");

        openBurgerMenu();

        onView(withContentDescription("Dashboard")).check(doesNotExist());
    }
    @Test
    public void testBurgerMenuHidesCurrentPage_EventList(){

        openBurgerMenu();
        clickMenuItem("Event List");

        openBurgerMenu();

        onView(withContentDescription("Event List")).check(doesNotExist());
    }

    @Test
    public void testBurgerMenuHidesCurrentPage_Information(){

        openBurgerMenu();
        clickMenuItem("Home");

        openBurgerMenu();
        clickMenuItem("Information");
        openBurgerMenu();

        onView(withContentDescription("Information")).check(doesNotExist());
    }


}
