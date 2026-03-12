package ca.quanta.quantaevents;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.FirebaseApp;

import java.util.Map;

import ca.quanta.quantaevents.burger.SmartBurger;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.databinding.ActivityMainBinding;
import ca.quanta.quantaevents.fragments.AccountFragment;
import ca.quanta.quantaevents.fragments.AdminPanelFragment;
import ca.quanta.quantaevents.fragments.EntrantEventListFragment;
import ca.quanta.quantaevents.fragments.EventDashboardFragment;
import ca.quanta.quantaevents.fragments.HomeFragment;
import ca.quanta.quantaevents.fragments.InformationFragment;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
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
                .with(new InformationFragment(), R.drawable.material_symbols_info_outline, NavGraphDirections::actionGlobalInformationFragment)
                .with(new EventDashboardFragment(), R.drawable.material_symbols_dashboard_outline, SmartBurger.ORGANIZER_GROUP, NavGraphDirections::actionGlobalEventdashboardFragment)
                .with(new EntrantEventListFragment(), R.drawable.material_symbols_event_list_outline, SmartBurger.ENTRANT_GROUP, NavGraphDirections::actionGlobalEntranteventlistFragment)
                .with(new AccountFragment(), R.drawable.material_symbols_person_outline, NavGraphDirections::actionGlobalAccountFragment)
                .with(new AdminPanelFragment(), R.drawable.material_symbols_security, SmartBurger.ADMIN_GROUP, NavGraphDirections::actionGlobalAdminpanelFragment)
                .with(new HomeFragment(), R.drawable.material_symbols_home_outline, NavGraphDirections::actionGlobalHomeFragment)
                .inject();

        sessionStore.observeSession(this, (userId, deviceId) -> {
            if (userId == null || deviceId == null) {
                sessionStore.setRoleMask(0);
                return;
            }
            userModel.getUserRaw(userId, deviceId)
                    .addOnSuccessListener(data -> sessionStore.setRoleMask(extractRoleMask(data)))
                    .addOnFailureListener(_ex -> sessionStore.setRoleMask(0));
        });

        sessionStore.getRoleMask().observe(this, mask -> burgerState.setGroupFilter(mask == null ? 0 : mask));
    }

    @SuppressWarnings("unchecked")
    private int extractRoleMask(Map<String, Object> userData) {
        int mask = 0;
        if (userData == null) {
            return mask;
        }
        Object entrant = userData.get("entrant");
        Object organizer = userData.get("organizer");
        Object admin = userData.get("admin");
        if (entrant instanceof Map) {
            mask |= SmartBurger.ENTRANT_GROUP;
        }
        if (organizer instanceof Map) {
            mask |= SmartBurger.ORGANIZER_GROUP;
        }
        if (admin instanceof Map) {
            mask |= SmartBurger.ADMIN_GROUP;
        }
        return mask;
    }
}
