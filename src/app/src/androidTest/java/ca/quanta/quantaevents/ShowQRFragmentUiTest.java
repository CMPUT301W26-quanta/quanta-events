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

import java.util.UUID;

import ca.quanta.quantaevents.fragments.ShowQRFragmentArgs;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShowQRFragmentUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        activityRule.getScenario().onActivity(FragmentUiTestUtils::setFakeSession);
    }

    @Test
    public void displaysQrImage() {
        activityRule.getScenario().onActivity(activity -> {
            ShowQRFragmentArgs args = new ShowQRFragmentArgs.Builder(UUID.randomUUID()).build();
            FragmentUiTestUtils.navigate(activity, R.id.showQRFragment, args.toBundle());
        });

        onView(withId(R.id.qr_code_image)).check(matches(isDisplayed()));
    }


    @After
    public void tearDown() {
        activityRule.getScenario().close();
    }

}