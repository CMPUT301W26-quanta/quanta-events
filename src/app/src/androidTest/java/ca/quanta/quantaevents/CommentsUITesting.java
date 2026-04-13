//package ca.quanta.quantaevents;
//
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
//import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
//import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
//import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//
//import static org.hamcrest.CoreMatchers.allOf;
//
//import android.view.View;
//
//import androidx.recyclerview.widget.RecyclerView;
//import androidx.test.espresso.UiController;
//import androidx.test.espresso.ViewAction;
//import androidx.test.espresso.contrib.RecyclerViewActions;
//import androidx.test.ext.junit.rules.ActivityScenarioRule;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import static androidx.test.espresso.action.ViewActions.scrollTo;
//import static org.hamcrest.CoreMatchers.not;
//
//
//import org.hamcrest.Matcher;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(AndroidJUnit4.class)
//
//public class CommentsUITesting {
//
//    @Rule
//    public ActivityScenarioRule<MainActivity> activityRule =
//            new ActivityScenarioRule<>(MainActivity.class);
//
//    @Before
//    public void navigateToEventDetails(){
//
//        try {
//
//            onView(withId(R.id.input_name)).perform(typeText("Test User"), closeSoftKeyboard());
//
//            String email = "test" + System.currentTimeMillis() + "@mail.com";
//
//            onView(withId(R.id.input_email)).perform(typeText(email), closeSoftKeyboard());
//
//            onView(withId(R.id.save_button)).perform(click());
//
//            onView(withId(R.id.coordinator)).check(matches(isDisplayed()));
//        } catch (Exception ignored) {
//
//        }
//
//        openBurgerMenu();
//
//        Matcher<View> item = allOf(withContentDescription("Event List"), isDescendantOfA(withId(R.id.coordinator)));
//
//
//        waitForView(item);
//
//        clickMenuItem("Event List");
//
//
//        waitForView(withId(R.id.search_button));
//
//        onView(withId(R.id.search_button)).perform(click());
//
//        waitForView(withId(R.id.events_recycler_view));
//
//        onView(withId(R.id.events_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
//
//    }
//
//    @Test
//    public void commentInputIsDisplayed(){
//        Matcher<View> commentInput = allOf(withId(R.id.type_comment), not(isDescendantOfA(withId(R.id.comments_recycler_view))));
//        onView(commentInput).check(matches(isDisplayed()));
//
//    }
//
//    @Test
//    public void typingCommentShowsTextInInput(){
//        Matcher<View> commentInput = allOf(withId(R.id.type_comment), not(isDescendantOfA(withId(R.id.comments_recycler_view))));
//        onView(commentInput).perform(scrollTo(), typeText("This is a test comment"), closeSoftKeyboard());
//        onView(commentInput).check(matches(withText("This is a test comment")));
//    }
//
//    @Test
//    public void postingCommentAppearsInList(){
//        String testMessage = "Hello this is a test comment";
//
//        onView(withId(R.id.type_comment)).perform(typeText(testMessage), closeSoftKeyboard());
//
//        onView(withId(R.id.send_comment)).perform(click());
//
//        waitForView(withId(R.id.comments_recycler_view));
//        onView(withId(R.id.comments_recycler_view)).check(matches(hasDescendant(withText(testMessage))));
//
//    }
//
//    @Test
//    public void sendButtonIsDisplayed(){
//        onView(withId(R.id.send_comment)).check(matches(isDisplayed()));
//
//    }
//
//    @Test
//    public void emptyCommentDoesNotPost(){
//
//        Matcher<View> commentInput = allOf(withId(R.id.type_comment), not(isDescendantOfA(withId(R.id.comments_recycler_view))));
//
//        onView(withId(R.id.send_comment)).perform(click());
//
//        onView(commentInput).check(matches(withText("")));
//    }
//
//
//
//    private void clickMenuItem(String label){
//        Matcher<View> item = allOf(withContentDescription(label), isDescendantOfA(withId(R.id.coordinator)));
//
//        onView(item).perform(click());
//    }
//
//
//
//    private void waitForView(Matcher<View> matcher){
//
//        long end = System.currentTimeMillis() + 7000;
//        while (System.currentTimeMillis() < end){
//            try {
//                onView(matcher).check(matches(isDisplayed()));
//                return;
//            } catch (Throwable e) {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ignored) {}
//            }
//        }
//        onView(matcher).check(matches(isDisplayed()));
//
//    }
//
//    private void openBurgerMenu(){
//        Matcher<View> burgerButton = allOf(withContentDescription("Open Menu"), isDescendantOfA(withId(R.id.coordinator)));
//
//        waitForView(burgerButton);
//        onView(burgerButton).perform(click());
//    }
//
//
//}
