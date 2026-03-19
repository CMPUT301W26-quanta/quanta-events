package ca.quanta.quantaevents.loading;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import ca.quanta.quantaevents.R;

/**
 * Loading screen display class
 */
public class Loader {
    private static class BackPressCallback extends OnBackPressedCallback {
        AppCompatActivity activity;

        BackPressCallback(AppCompatActivity activity) {
            super(false);
            this.activity = activity;
        }

        @Override
        public void handleOnBackPressed() {
            activity.finishAffinity();
        }
    }

    private final AppCompatActivity activity;
    private final FrameLayout layout;
    private ProgressBar progressBar;
    private final BackPressCallback backPressCallback;

    public Loader(AppCompatActivity activity, FrameLayout layout) {
        this.activity = activity;
        this.layout = layout;
        backPressCallback = new BackPressCallback(activity);
        new ViewModelProvider(activity).get(LoaderState.class).getActive().observe(activity, this::setActive);
    }

    public void inject() {
        layout.setBackgroundColor(ResourcesCompat.getColor(this.activity.getResources(), R.color.color_light, this.activity.getTheme()));
        progressBar = new ProgressBar(this.activity);
        progressBar.setLayoutParams(new FrameLayout.LayoutParams(dpToPixels(64), dpToPixels(64), Gravity.CENTER));
        layout.addView(progressBar);
        layout.setVisibility(GONE);
        activity.getOnBackPressedDispatcher().addCallback(backPressCallback);
    }

    private void setActive(boolean active) {
        if (active) {
            layout.setVisibility(VISIBLE);
            if (progressBar != null) {
                progressBar.setAlpha(0.0f);
                progressBar.animate().alpha(1.0f).setDuration(175).setStartDelay(150);
            }
            layout.setAlpha(0.0f);
            layout.animate().alpha(1.0f).setDuration(125);
        } else {
            layout.animate().alpha(0.0f).setDuration(125).withEndAction(() -> layout.setVisibility(GONE));
        }
        backPressCallback.setEnabled(active);
    }

    private int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, activity.getResources().getDisplayMetrics());
    }
}

