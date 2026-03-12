package ca.quanta.quantaevents.burger;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.R;

public class SmartBurger {
    public interface Navigator {
        NavDirections getDirections();
    }

    public final static int ENTRANT_GROUP = 0b001;
    public final static int ORGANIZER_GROUP = 0b010;
    public final static int ADMIN_GROUP = 0b100;

    static class ItemData {
        final Navigator navigator;
        @DrawableRes
        final int icon;
        final int order;

        @Nullable
        final Integer group;

        ItemData(Navigator navigator, @DrawableRes int icon, int order) {
            this.navigator = navigator;
            this.icon = icon;
            this.order = order;
            group = null;
        }

        ItemData(Navigator navigator, @DrawableRes int icon, int group, int order) {
            this.navigator = navigator;
            this.icon = icon;
            this.order = order;
            this.group = group;
        }

        Navigator getNavigator() {
            return navigator;
        }

        @DrawableRes
        int getIcon() {
            return icon;
        }

        int getOrder() {
            return order;
        }

        @Nullable
        public Integer getGroup() {
            return group;
        }
    }

    private final AppCompatActivity activity;
    private final CoordinatorLayout layout;
    private final NavController navController;
    private final HashMap<UUID, ItemData> items;
    private ArrayList<ImageButton> displayedItems;

    private ImageButton rootButton;

    private boolean open;

    public SmartBurger(AppCompatActivity activity, NavController navController, CoordinatorLayout layout) {
        items = new HashMap<>();
        displayedItems = new ArrayList<>();
        open = false;
        this.activity = activity;
        this.layout = layout;
        this.navController = navController;
        new ViewModelProvider(activity).get(SmartBurgerState.class).getDisplay().observe(activity, this::onDisplayChanged);
        navController.addOnDestinationChangedListener((_nc, _nd, _b) -> this.changedDestination());
    }

    /**
     * Registers a given tagged fragment to the list of items in the smart burger menu, with no group association
     *
     * @param fragment  The fragment to register
     * @param icon      The icon to associate with this fragment
     * @param navigator The {@link NavDirections} provider to navigate to the given fragment
     * @return The same {@link SmartBurger} to allow method chaining
     */
    public <F extends Fragment & Tagged> SmartBurger with(F fragment, @DrawableRes int icon, Navigator navigator) {
        items.put(fragment.getUniqueTag(), new ItemData(navigator, icon, this.items.size()));
        return this;
    }

    /**
     * Registers a given tagged fragment to the list of items in the smart burger menu, with a provided group
     *
     * @param fragment  The fragment to register
     * @param icon      The icon to associate with this fragment
     * @param group     The group of this fragment. Should be either the bitmasks {@link #ENTRANT_GROUP}, {@link #ORGANIZER_GROUP}, or {@link #ADMIN_GROUP}
     * @param navigator The {@link NavDirections} provider to navigate to the given fragment
     * @return The same {@link SmartBurger} to allow method chaining
     */
    public <F extends Fragment & Tagged> SmartBurger with(F fragment, @DrawableRes int icon, int group, Navigator navigator) {
        items.put(fragment.getUniqueTag(), new ItemData(navigator, icon, group, this.items.size()));
        return this;
    }

    public void inject() {
        rootButton = createButton(R.drawable.material_symbols_menu);
        rootButton.setOnClickListener(_view -> this.toggle());
        rootButton.setVisibility(GONE);
        layout.addView(rootButton);
    }

    private void changedDestination() {
        new ViewModelProvider(activity).get(SmartBurgerState.class).hide();
    }

    private void onDisplayChanged(Boolean display) {
        if (display) {
            rebuild();
            toClosed();
            rootButton.setAlpha(0.0f);
            rootButton.setScaleX(0.8f);
            rootButton.setScaleY(0.8f);
            rootButton.setVisibility(VISIBLE);
            rootButton.animate()
                    .alpha(1.0f)
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(200)
                    .start();
        } else {
            rootButton.setVisibility(GONE);
            for (ImageButton button : displayedItems) {
                layout.removeView(button);
            }
            displayedItems = new ArrayList<>();
        }
    }

    private void toggle() {
        this.open = !this.open;
        Drawable drawable;
        if (this.open) {
            rebuild();
            drawable = getDrawable(R.drawable.menu_to_close);
            for (int i = 0; i < displayedItems.size(); i++) {
                showButton(displayedItems.get(i), i);
            }
        } else {
            drawable = getDrawable(R.drawable.close_to_menu);
            for (int i = 0; i < displayedItems.size(); i++) {
                hideButton(displayedItems.get(i), displayedItems.size() - i - 1);
            }
        }
        rootButton.setImageDrawable(drawable);
        ((Animatable) drawable).start();
    }

    private void toClosed() {
        this.open = false;
        rootButton.setImageDrawable(getDrawable(R.drawable.material_symbols_menu));
    }

    private void rebuild() {
        UUID active = new ViewModelProvider(activity).get(SmartBurgerState.class).getActive();
        int groupFilter = new ViewModelProvider(activity).get(SmartBurgerState.class).getGroupFilter();
        ArrayList<Map.Entry<UUID, ItemData>> itemArray = new ArrayList<>(items.entrySet());
        itemArray.sort(Comparator.comparingInt(a -> a.getValue().getOrder()));
        for (ImageButton button : displayedItems) {
            layout.removeView(button);
        }
        displayedItems = new ArrayList<>();
        for (Map.Entry<UUID, ItemData> entry : itemArray) {

            if (entry.getKey() != active) {
                Integer group = entry.getValue().getGroup();
                if (group == null || ((group & groupFilter) > 0)) {
                    ImageButton button = createButton(entry.getValue().getIcon());
                    button.setScaleX(0.8f);
                    button.setScaleY(0.8f);
                    button.setAlpha(0.8f);
                    button.setVisibility(GONE);
                    button.setOnClickListener(_view -> navController.navigate(entry.getValue().getNavigator().getDirections()));
                    displayedItems.add(button);
                    layout.addView(button);
                }

            }
        }
        rootButton.bringToFront();
    }

    private ImageButton createButton(@DrawableRes int icon) {
        ImageButton button = new ImageButton(activity);
        button.setBackground(getDrawable(R.drawable.rounded_border_box_ripple));
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin = dpToPixels(24);
        params.setMargins(margin, margin, margin, margin);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        button.setLayoutParams(params);
        button.setImageDrawable(getDrawable(icon));
        int padding = dpToPixels(14);
        button.setPadding(padding, padding, padding, padding);
        return button;
    }

    private void showButton(ImageButton button, int index) {
        button.setVisibility(VISIBLE);
        button.setAlpha(0.0f);
        button.setTranslationY(0.0f);
        button.animate()
                .translationY(-((index + 1) * rootButton.getHeight() + dpToPixels(4)))
                .alpha(1.0f)
                .setDuration(200)
                .setStartDelay(index * 25L)
                .start();
    }

    private void hideButton(ImageButton button, int index) {
        button.animate()
                .translationY(0)
                .alpha(0.0f)
                .setDuration(200)
                .setStartDelay(index * 25L)
                .withEndAction(() -> button.setVisibility(GONE))
                .start();
    }

    private Drawable getDrawable(@DrawableRes int drawable) {
        return ResourcesCompat.getDrawable(activity.getResources(), drawable, activity.getTheme());
    }

    private @ColorInt int getColor(@ColorRes int color) {
        return ResourcesCompat.getColor(activity.getResources(), color, activity.getTheme());
    }

    private int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, activity.getResources().getDisplayMetrics());
    }
}
