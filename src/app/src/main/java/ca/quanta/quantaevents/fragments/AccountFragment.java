package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
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

import ca.quanta.quantaevents.utils.ThemeSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentAccountBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.User;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

/**
 * Fragment for displaying UI for editing your own account.
 */
public class AccountFragment extends Fragment implements Tagged {
    private FragmentAccountBinding binding;
    private SessionStore sessionStore;
    private UserViewModel userModel;
    private UUID userId;
    private UUID deviceId;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // sets title as account
        // sets subtitle and the icon
        infoStore.setTitle("Account");
        infoStore.setSubtitle("Change account details");
        infoStore.setIconRes(R.drawable.material_symbols_person_outline);

        this.sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        this.userModel = new ViewModelProvider(this).get(UserViewModel.class);

        // verify and check session
        this.sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            this.userId = uid;
            this.deviceId = did;
            maybeLoadUser();
        });
        binding.switchDarkMode.setChecked(ThemeSwitch.isDarkMode(requireContext()));
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeSwitch.setDarkMode(requireContext(), isChecked);
        });
        // set on click listener for back button
        this.binding.deleteButton.setOnClickListener(
                v -> {
                    deleteUser();
                }
        );

        this.binding.saveButton.setOnClickListener(
                v -> {
                    updateUser();
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Tries to load a user and populates fields if successful.
     */
    private void maybeLoadUser() {
        if (userId == null || deviceId == null) {
            return;
        }
        userModel.getUser(userId, deviceId)
                .addOnSuccessListener(this::populateFields)
                .addOnFailureListener(ex -> {
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                    }
                }).addOnCanceledListener(this::handleMissingUser);
    }

    /**
     * Sets input fields to the data fetched from the server.
     * @param user User whose data is used to populate input fields.
     */
    private void populateFields(@NonNull User user) {
        if (!isAdded() || binding == null) return;
        System.out.println(user);

        binding.inputName.setText(user.getName());
        binding.inputEmail.setText(user.getEmail());

        binding.inputPhone.setText(user.getPhoneNumber());

        User.Entrant entrant = user.getEntrant();
        if (entrant != null) {
            binding.checkNotifications.setChecked(entrant.getReceiveNotifications());
        }
    }

    /**
     * Checks if a user is not found.
     * @param ex Exception to be checked.
     * @return true if argument is a not found exception, false otherwise.
     */
    private boolean isUserNotFound(Exception ex) {
        if (ex instanceof FirebaseFunctionsException) {
            FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) ex).getCode();
            return code == FirebaseFunctionsException.Code.NOT_FOUND;
        } else {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Deletes a user from the database and redirects to register fragment.
     */
    private void deleteUser() {
        if (userId == null || deviceId == null) {
            return;
        }
        binding.deleteButton.setEnabled(false);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
                userModel.deleteUser(userId, deviceId, userId)
                        .addOnSuccessListener(_done -> {
                            if (!isAdded() || binding == null) return;
                            sessionStore.clearSession();
                            binding.deleteButton.setEnabled(true);
                            ToastManager.show(getContext(), "Account deleted", Toast.LENGTH_LONG);
                            if (isAdded()) {
                                NavDirections action = AccountFragmentDirections.actionAccountfragmentToRegisterfragment();
                                Navigation.findNavController(requireView()).navigate(action);
                            }
                        })
                        .addOnFailureListener(ex -> {
                            if (!isAdded() || binding == null) return;
                            binding.deleteButton.setEnabled(true);
                            ToastManager.show(getContext(), "Failed to delete account", Toast.LENGTH_LONG);
                        })
        );
    }

    /**
     * Updates user details in the database and redirects to home fragment on success.
     */
    private void updateUser() {
        if (userId == null || deviceId == null) {
            return;
        }
        String name = binding.inputName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String phone = binding.inputPhone.getText().toString().trim();

        name = name.isEmpty() ? null : name;
        email = email.isEmpty() ? null : email;
        phone = phone.isEmpty() ? null : phone;

        if (name == null) {
            binding.layoutName.setError("Name is required");
            binding.inputName.requestFocus();
            return;
        }
        if (email == null) {
            binding.layoutEmail.setError("Email is required");
            binding.inputEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutEmail.setError("Enter a valid email address");
            binding.inputEmail.requestFocus();
            return;
        }

        Boolean receiveNotifications = binding.checkNotifications.isChecked();

        binding.saveButton.setEnabled(false);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
                userModel.updateUser(userId, deviceId, name, email, phone, receiveNotifications)
                        .addOnSuccessListener(_userId -> {
                            if (!isAdded() || binding == null) return;
                            binding.saveButton.setEnabled(true);
                            ToastManager.show(getContext(), "Account updated", Toast.LENGTH_LONG);
                            NavDirections action = AccountFragmentDirections.actionAccountfragmentToHomefragment();
                            Navigation.findNavController(requireView()).navigate(action);
                        })
                        .addOnFailureListener(ex -> {
                            if (!isAdded() || binding == null) return;
                            binding.saveButton.setEnabled(true);
                            ToastManager.show(getContext(), "Failed to update account", Toast.LENGTH_LONG);
                        })
        );
    }

    /**
     * Clears the session and redirects to registration fragment if the user does not exist.
     */
    private void handleMissingUser() {
        sessionStore.clearSession();
        System.out.println("MISSING USER");
        if (isAdded()) {
            NavDirections action = AccountFragmentDirections.actionAccountfragmentToRegisterfragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    private static final UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
