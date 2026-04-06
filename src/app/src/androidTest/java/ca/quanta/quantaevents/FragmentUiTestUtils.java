package ca.quanta.quantaevents;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import java.util.UUID;

import ca.quanta.quantaevents.burger.SmartBurger;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.stores.SessionStore;

public final class FragmentUiTestUtils {
    private FragmentUiTestUtils() {
    }

    public static void setFakeSession(MainActivity activity) {
        SessionStore store = new ViewModelProvider(activity).get(SessionStore.class);
        store.setSession(UUID.randomUUID(), UUID.randomUUID());
        setAllBurgerGroups(activity);
    }

    public static void clearSession(MainActivity activity) {
        SessionStore store = new ViewModelProvider(activity).get(SessionStore.class);
        store.clearSession();
        store.setRoleMask(0);
    }

    public static void setAllBurgerGroups(MainActivity activity) {
        int allGroups = SmartBurger.ENTRANT_GROUP | SmartBurger.ORGANIZER_GROUP | SmartBurger.ADMIN_GROUP;
        SessionStore store = new ViewModelProvider(activity).get(SessionStore.class);
        store.setRoleMask(allGroups);
        new ViewModelProvider(activity).get(SmartBurgerState.class).setGroupFilter(allGroups);
    }

    public static void navigate(MainActivity activity, int destinationId, @Nullable Bundle args) {
        NavController navController = Navigation.findNavController(activity, R.id.nav_host);
        if (args == null) {
            navController.navigate(destinationId);
        } else {
            navController.navigate(destinationId, args);
        }
    }
}
