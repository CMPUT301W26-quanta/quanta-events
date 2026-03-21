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
import androidx.navigation.Navigation;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentNotificationEditorBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.NotificationViewModel;

public class EventNotificationEditorFragment extends Fragment {

    private FragmentNotificationEditorBinding binding;
    private NotificationViewModel model;
    private UUID userId;
    private UUID deviceId;
    private SessionStore sessionStore;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Notification Editor");
        infoStore.setSubtitle("Send a push notification");
        infoStore.setIconRes(R.drawable.material_symbols_notification_add_outline);

        // Setting up view models and session store stuff
        model = new ViewModelProvider(this).get(NotificationViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);

        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
        });

        //set on click listener for the back button
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        binding.saveButton.setOnClickListener(_view -> {
            Log.d("TAG", userId.toString());
            Log.d("TAG", deviceId.toString());

            // Testing some hard-coded values
            // Note: currently, this call to createNotification doesn't seem to do anything
            // Will need to check this, and the cloud function
            // Giving a 'missing required fields' error
            model.createNotification(userId, deviceId, "This is a message",
                    "This is a title", "71b989cf-b802-4f0a-919c-efad7b283621",
                    true, true, true)
                    .addOnSuccessListener(notificationId -> {
                        binding.saveButton.setEnabled(true);
                        Toast.makeText(requireContext(), "Notification created", Toast.LENGTH_LONG).show();
                        if (isAdded()) {
                            Navigation.findNavController(requireView()).popBackStack();
                        }
                    })
                    .addOnFailureListener(ex -> {
                        binding.saveButton.setEnabled(true);
                        Log.e("TAG", "Failed to create notification", ex);
                        Toast.makeText(requireContext(), "Failed to create notification", Toast.LENGTH_LONG).show();
                    });

        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentNotificationEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
