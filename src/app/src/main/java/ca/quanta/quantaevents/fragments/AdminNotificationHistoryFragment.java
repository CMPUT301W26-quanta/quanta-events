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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.NotificationAdapter;
import ca.quanta.quantaevents.databinding.FragmentAdminNotificationHistoryBinding;
import ca.quanta.quantaevents.models.Notification;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.NotificationViewModel;

public class AdminNotificationHistoryFragment extends Fragment {
    private FragmentAdminNotificationHistoryBinding binding;
    private NotificationViewModel notificationModel;
    private UUID userId;
    private UUID deviceId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // **** set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);

        infoStore.setTitle("Notification History");
        infoStore.setSubtitle("View user notification history.");
        infoStore.setIconRes(R.drawable.material_symbols_history);

        // **** set up the args

        AdminNotificationHistoryFragmentArgs args = AdminNotificationHistoryFragmentArgs.fromBundle(this.getArguments());

        // **** set up the view models

        this.notificationModel = new ViewModelProvider(this.requireActivity()).get(NotificationViewModel.class);

        // **** set up the session store

        SessionStore sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);

        sessionStore.observeSession(getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;

            // set up the notifications recycler view once the userId and deviceId are ready
            listNotifications(args.getProfileId());
        });

        // **** set up the back buttons on click listener

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    private void listNotifications(UUID profileID) {
        // display the notifications belonging to the profile

        this.notificationModel.getAllNotifications(this.userId, this.deviceId, profileID)
                .addOnSuccessListener(this::getAndDisplayNotifications)
                .addOnFailureListener(exception -> {
                    Log.e("AdminNotificationHistoryFragment", "Failed to fetch notifications.", exception);

                    ToastManager.show(getContext(), "Failed to fetch notifications", Toast.LENGTH_LONG);

                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("AdminNotificationHistoryFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });
    }

    private void getAndDisplayNotifications(ArrayList<Notification> notifications) {
        // **** use the adapter to display them

        NotificationAdapter notificationAdapter = new NotificationAdapter(notifications);

        // **** set up the notification recycler view

        binding.notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.notificationsRecyclerView.setAdapter(notificationAdapter);

        // **** display toast in case there are none

        if (notifications.isEmpty()) {
            ToastManager.show(getContext(), "No notifications to display.", Toast.LENGTH_LONG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminNotificationHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
