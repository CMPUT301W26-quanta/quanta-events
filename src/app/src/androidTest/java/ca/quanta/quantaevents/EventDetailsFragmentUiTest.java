package ca.quanta.quantaevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.Manifest;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ca.quanta.quantaevents.fragments.EventDetailsFragmentArgs;
import ca.quanta.quantaevents.fragments.EventDetailsFragment;
import org.junit.After;

@RunWith(AndroidJUnit4.class)
public class EventDetailsFragmentUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        activityRule.getScenario().onActivity(FragmentUiTestUtils::setFakeSession);
    }

    @Test
    public void displaysScrollView() {
        CountDownLatch viewReady = new CountDownLatch(1);
        activityRule.getScenario().onActivity(activity -> {
            EventDetailsFragmentArgs args =
                    new EventDetailsFragmentArgs.Builder(UUID.randomUUID()).setFromAdmin(false).build();
            FragmentUiTestUtils.navigate(activity, R.id.eventDetailsFragment, args.toBundle());
            Fragment navHost = activity.getSupportFragmentManager().findFragmentById(R.id.nav_host);
            if (navHost instanceof NavHostFragment) {
                FragmentManager childManager = ((NavHostFragment) navHost).getChildFragmentManager();
                childManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, android.os.Bundle savedInstanceState) {
                        if (f instanceof EventDetailsFragment) {
                            v.setVisibility(View.VISIBLE);
                            fm.unregisterFragmentLifecycleCallbacks(this);
                            viewReady.countDown();
                        }
                    }
                }, false);
            }
        });

        try {
            if (!viewReady.await(5, TimeUnit.SECONDS)) {
                throw new AssertionError("EventDetailsFragment view was not created in time.");
            }
        } catch (InterruptedException e) {
            throw new AssertionError("Interrupted while waiting for EventDetailsFragment view.", e);
        }

        onView(withId(R.id.scroll_view)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        activityRule.getScenario().close();
    }

}
