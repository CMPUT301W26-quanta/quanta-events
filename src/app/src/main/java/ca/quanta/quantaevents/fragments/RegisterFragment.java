package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import java.util.Optional;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentRegisterBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.UserViewModel;


/**
 * Class which defines the registration screen.
 */
public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private static final String TAG = "RegisterFragment";
    private UserViewModel model;

    private SessionStore sessionStore;
    private boolean redirectedToHome = false;

    /**
     * Constructor for a RegisterFragment object.
     */
    public RegisterFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        observeSessionAndRedirect();

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Welcome! and also the subtitle
        // also sets the icon for the page
        infoStore.setTitle("Welcome!");
        infoStore.setSubtitle("Get started by creating an account.");
        infoStore.setIconRes(R.drawable.material_symbols_waving_hand_outline);

        model = new ViewModelProvider(requireActivity()).get(UserViewModel.class);


        // **** set up buttons

        binding.saveButton.setOnClickListener(this::onSaveClick);
    }

    /**
     * Listener for the save button that extracts text input and check box values..
     * and decides what to do on success or on failure
     */
    public void onSaveClick(View v) {
        if (redirectedToHome) {
            return;
        }
        String name = normalizeEmpty(Optional.ofNullable(binding.inputName.getText()).map(e -> e.toString().trim()).orElse(null));
        String email = normalizeEmpty(Optional.ofNullable(binding.inputEmail.getText()).map(e -> e.toString().trim()).orElse(null));
        String phone = normalizeEmpty(Optional.ofNullable(binding.inputPhone.getText()).map(e -> e.toString().trim()).orElse(null));

        Boolean isEntrant = binding.checkEntrant.isChecked();
        Boolean isOrganizer = binding.checkOrganizer.isChecked();
        Boolean isAdmin = binding.checkAdmin.isChecked();
        Boolean getNotifications = binding.checkNotifications.isChecked();
        UUID deviceId = UUID.randomUUID();
        binding.saveButton.setEnabled(false);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
                model.createUser(name, email, phone, isEntrant, isOrganizer, isAdmin, getNotifications, deviceId)
                        .addOnSuccessListener(userId -> {
                            sessionStore.setSession(userId, deviceId);
                            binding.saveButton.setEnabled(true);
                            ToastManager.show(getContext(), "Account created", Toast.LENGTH_LONG);
                            tryRedirect();
                        })
                        .addOnFailureListener(ex -> {
                            binding.saveButton.setEnabled(true);
                            Log.e(TAG, "Failed to create account", ex);
                            ToastManager.show(getContext(), "Failed to create account", Toast.LENGTH_LONG);
                        })
        );

    }

    // Regidrect the user to appropriate fragment
    private void observeSessionAndRedirect() {
        sessionStore.getUserId().observe(getViewLifecycleOwner(), userId -> tryRedirect());
        sessionStore.getDeviceId().observe(getViewLifecycleOwner(), deviceId -> tryRedirect());
    }

    @Nullable
    static String normalizeEmpty(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }

    // triess redirecting user to appropriate fragment.
    private void tryRedirect() {
        if (redirectedToHome) {
            return;
        }
        if (sessionStore.hasSession() && isAdded()) {
            androidx.navigation.NavController navController = Navigation.findNavController(requireView());
            if (navController.getCurrentDestination() == null
                    || navController.getCurrentDestination().getId() != R.id.registerFragment) {
                return;
            }
            redirectedToHome = true;
            NavDirections action = RegisterFragmentDirections.actionRegisterfragmentToHomefragment();
            navController.navigate(action);
        }
    }
}
