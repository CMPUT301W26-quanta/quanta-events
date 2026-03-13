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
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.NotificationAdapter;
import ca.quanta.quantaevents.databinding.FragmentAdminNotificationHistoryBinding;
import ca.quanta.quantaevents.models.Notification;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class AdminNotificationHistoryFragment extends Fragment {
    private FragmentAdminNotificationHistoryBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);

        infoStore.setTitle("Notification History");
        infoStore.setSubtitle("View event notification history.");
        infoStore.setIconRes(R.drawable.material_symbols_history);

        // set up the notification recycler view

        // TODO: fetch all notifications from the database instead

        List<Notification> notifications = new ArrayList<Notification>();

        notifications.add(new Notification(null, null, "Hotdog Eating Contest Notice", "Event starting soon! Remember that refreshments are provided. Make sure to bring a friend!"));
        notifications.add(new Notification(null, null, "Hotdog Condiments", "Relish is overrated. For that reason, only ketchup and mustard will be provided during the contest. Sorry not sorry!"));

        // use the adapter to display them

        NotificationAdapter notificationAdapter = new NotificationAdapter(notifications);

        binding.notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.notificationsRecyclerView.setAdapter(notificationAdapter);

        // set up the back buttons on click listener

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminNotificationHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
