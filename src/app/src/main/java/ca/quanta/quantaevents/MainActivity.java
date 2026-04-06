package ca.quanta.quantaevents;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;

import ca.quanta.quantaevents.burger.SmartBurger;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.databinding.ActivityMainBinding;
import ca.quanta.quantaevents.fragments.AccountFragment;
import ca.quanta.quantaevents.fragments.AdminPanelFragment;
import ca.quanta.quantaevents.fragments.EntrantEventListFragment;
import ca.quanta.quantaevents.fragments.EventDashboardFragment;
import ca.quanta.quantaevents.fragments.HomeFragment;
import ca.quanta.quantaevents.loading.Loader;
import ca.quanta.quantaevents.models.User;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ThemeSwitch;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

/**
 * Entry point of app, sets up appropriate buttons for smart burger menu based on user role
 * this where all the fragments will load up after the end of infoStore view
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        // our app is not designed to support dark mode properly.
        // eg. the background becomes dark, but the text remains dark so you can't read it
        // So force light mode
        ThemeSwitch.applyTheme(this);
        setContentView(binding.getRoot());

        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException |
                 GooglePlayServicesNotAvailableException e) {
            throw new RuntimeException(e);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);

        FragmentInfoStore infoStore = new ViewModelProvider(this).get(FragmentInfoStore.class);
        SessionStore sessionStore = new ViewModelProvider(this).get(SessionStore.class);
        UserViewModel userModel = new ViewModelProvider(this).get(UserViewModel.class);
        SmartBurgerState burgerState = new ViewModelProvider(this).get(SmartBurgerState.class);

        infoStore.getTitle()
                .observe(
                        this,
                        binding.titleView::setText
                );

        infoStore.getSubtitle()
                .observe(
                        this,
                        binding.subtitleView::setText
                );

        infoStore.getIconRes()
                .observe(
                        this,
                        iconRes -> binding.iconView.setImageDrawable(AppCompatResources.getDrawable(this, iconRes))
                );

        new SmartBurger(this, ((NavHostFragment) binding.navHost.getFragment()).getNavController(), binding.coordinator)
                .with(new EventDashboardFragment(), R.drawable.material_symbols_dashboard_outline, SmartBurger.ORGANIZER_GROUP, "Dashboard", NavGraphDirections::actionGlobalEventdashboardFragment)
                .with(new EntrantEventListFragment(), R.drawable.material_symbols_event_list_outline, SmartBurger.ENTRANT_GROUP, "Event List", NavGraphDirections::actionGlobalEntranteventlistFragment)
                .with(new AccountFragment(), R.drawable.material_symbols_person_outline, "Account", NavGraphDirections::actionGlobalAccountFragment)
                .with(new AdminPanelFragment(), R.drawable.material_symbols_security, SmartBurger.ADMIN_GROUP, "Admin Panel", NavGraphDirections::actionGlobalAdminpanelFragment)
                .with(new HomeFragment(), R.drawable.material_symbols_home_outline, "Home", NavGraphDirections::actionGlobalHomeFragment)
                .inject();

        sessionStore.observeSession(this, (userId, deviceId) -> {
            if (userId == null || deviceId == null) {
                sessionStore.setRoleMask(0);
                return;
            }
            userModel.getUser(userId, deviceId)
                    .addOnSuccessListener(data -> sessionStore.setRoleMask(extractRoleMask(data)))
                    .addOnFailureListener(_ex -> sessionStore.setRoleMask(0));
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(
                    token -> NotificationService.updateToken(token, userId.toString(), deviceId.toString()).addOnFailureListener(Throwable::printStackTrace)
            );
        });

        sessionStore.getRoleMask().observe(this, mask -> burgerState.setGroupFilter(mask == null ? 0 : mask));

        new Loader(this, binding.loadingFrame).inject();

        requestNotificationPermission();
    }

    @SuppressWarnings("unchecked")
    private int extractRoleMask(User userData) {
        int mask = 0;
        if (userData == null) {
            return mask;
        }
        Boolean entrant = userData.isEntrant();
        Boolean organizer = userData.isOrganizer();
        Boolean admin = userData.isAdmin();
        if (entrant) {
            mask |= SmartBurger.ENTRANT_GROUP;
        }
        if (organizer) {
            mask |= SmartBurger.ORGANIZER_GROUP;
        }
        if (admin) {
            mask |= SmartBurger.ADMIN_GROUP;
        }
        return mask;
    }

    /**
     * Requests permissions to receive notifications from the app.
     */
    private void requestNotificationPermission() {
        Boolean hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) {
            String[] perm = {Manifest.permission.POST_NOTIFICATIONS};
            ActivityCompat.requestPermissions(this, perm, 0);
        }
    }

}
