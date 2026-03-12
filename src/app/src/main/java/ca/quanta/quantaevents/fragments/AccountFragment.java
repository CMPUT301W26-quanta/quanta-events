package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.Map;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentAccountBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

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
        infoStore.setTitle("Account");
        infoStore.setSubtitle("Change account details");
        infoStore.setIconRes(R.drawable.material_symbols_person_outline);

        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        userModel = new ViewModelProvider(this).get(UserViewModel.class);
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            maybeLoadUser();
        });

        binding.deleteButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_accountfragment_to_registerfragment)
        );
        binding.saveButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_accountfragment_to_homefragment)
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void maybeLoadUser() {
        if (userId == null || deviceId == null) {
            return;
        }
        userModel.getUserRaw(userId, deviceId)
                .addOnSuccessListener(this::populateFields)
                .addOnFailureListener(ex -> {
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void populateFields(Map<String, Object> userData) {
        if (userData == null) {
            return;
        }

        binding.inputName.setText(stringValue(userData.get("name")));
        binding.inputEmail.setText(stringValue(userData.get("email")));
        binding.inputPhone.setText(stringValue(userData.get("phone")));

        Object entrant = userData.get("entrant");
        Object organizer = userData.get("organizer");
        Object admin = userData.get("admin");

        binding.checkEntrant.setChecked(entrant instanceof Map);
        binding.checkOrganizer.setChecked(organizer instanceof Map);
        binding.checkAdmin.setChecked(admin instanceof Map);

        boolean receiveNotifications = false;
        if (entrant instanceof Map) {
            Object receive = ((Map<String, Object>) entrant).get("receiveNotifications");
            if (receive instanceof Boolean) {
                receiveNotifications = (Boolean) receive;
            }
        }
        binding.checkNotifications.setChecked(receiveNotifications);
    }

    private static String stringValue(Object value) {
        if (value == null) {
            return "";
        }
        String result = value.toString().trim();
        return result;
    }

    private static UUID parseUUID(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isUserNotFound(Exception ex) {
        if (ex instanceof FirebaseFunctionsException) {
            FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) ex).getCode();
            return code == FirebaseFunctionsException.Code.NOT_FOUND;
        }
        return false;
    }

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
