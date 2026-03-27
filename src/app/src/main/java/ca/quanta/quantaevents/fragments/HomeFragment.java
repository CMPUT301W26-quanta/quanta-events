package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentHomeBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class HomeFragment extends Fragment implements Tagged {
    private FragmentHomeBinding binding;
    private SessionStore sessionStore;
    private UserViewModel userModel;
    private UUID userId;
    private UUID deviceId;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Home and subtitle to what the page does
        // also sets the icon for the page
        infoStore.setTitle("Home");
        infoStore.setSubtitle("Receive lottery selection updates here.");
        infoStore.setIconRes(R.drawable.material_symbols_home_outline);

        // manages user session which checks if user is registered
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        userModel = new ViewModelProvider(this).get(UserViewModel.class);
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            this.userId = uid;
            this.deviceId = did;

            maybeValidateUser();
        });

        // listener for info button

        binding.infoButton.setOnClickListener(_view -> {
            NavDirections action = HomeFragmentDirections.actionHomeFragmentToInformationFragment();
            Navigation.findNavController(requireView()).navigate(action);
        });

        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    //validates user by checking if they
    // exist in database if not handle it properly.
    private void maybeValidateUser() {
        if (userId == null || deviceId == null) {
            return;
        }
        userModel.getUser(userId, deviceId)
                .addOnFailureListener(ex -> {
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                    }
                })
                .addOnCanceledListener(this::handleMissingUser);
    }

    private boolean isUserNotFound(Exception ex) {
        if (ex instanceof FirebaseFunctionsException) {
            FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) ex).getCode();
            return code == FirebaseFunctionsException.Code.NOT_FOUND;
        }
        throw new RuntimeException(ex);
    }

    // clears shared_pereferencs file if the userid does not exist on database
    private void handleMissingUser() {
        sessionStore.clearSession();
        if (isAdded()) {
            Navigation.findNavController(requireView()).navigate(R.id.registerFragment);
        }
    }

    private static final UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }
}
