package ca.quanta.quantaevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegisterFragmentUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        activityRule.getScenario().onActivity(FragmentUiTestUtils::clearSession);
    }

    @Test
    public void displaysScrollView() {
        activityRule.getScenario().onActivity(activity ->
                FragmentUiTestUtils.navigate(activity, R.id.registerFragment, null)
        );

        onView(withId(R.id.scroll_view)).check(matches(isDisplayed()));
    }


    @After
    public void tearDown() {
        activityRule.getScenario().close();
    }

}