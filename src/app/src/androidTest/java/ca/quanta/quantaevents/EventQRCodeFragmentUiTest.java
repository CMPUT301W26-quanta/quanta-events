package ca.quanta.quantaevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.Manifest;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventQRCodeFragmentUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule cameraPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Before
    public void setUp() {
        activityRule.getScenario().onActivity(FragmentUiTestUtils::setFakeSession);
    }

    @Test
    public void displaysBackButton() {
        activityRule.getScenario().onActivity(activity ->
                FragmentUiTestUtils.navigate(activity, R.id.eventQRCodeFragment, null)
        );

        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        activityRule.getScenario().close();
    }

}