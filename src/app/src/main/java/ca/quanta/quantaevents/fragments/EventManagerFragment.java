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

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventManagerBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventManagerFragment extends Fragment {
    private FragmentEventManagerBinding binding;
    private String eventId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Manager");
        infoStore.setSubtitle("Manage your Events");
        infoStore.setIconRes(R.drawable.material_symbols_edit_outline);

        readEventId();

        binding.editDetailsButton.setOnClickListener(
                v -> {
                    Bundle args = new Bundle();
                    if (eventId != null) {
                        args.putString("eventId", eventId);
                    }
                    Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_createeditorfragment, args);
                }
        );
        binding.shareQrButton.setOnClickListener(
                v -> {
                    Bundle args = new Bundle();
                    if (eventId != null) {
                        args.putString("eventId", eventId);
                    }
                    Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_showqrfragment, args);
                }
        );
        binding.viewWaitListButton.setOnClickListener(
                v -> {
                    Bundle args = new Bundle();
                    if (eventId != null) {
                        args.putString("eventId", eventId);
                    }
                    Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_eventwaitinglistfragment, args);
                }
        );
        binding.sendNotificationButton.setOnClickListener(
                v -> {
                    Bundle args = new Bundle();
                    if (eventId != null) {
                        args.putString("eventId", eventId);
                    }
                    Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_sendnotificationfragment, args);
                }
        );
        binding.backButton.setOnClickListener(
                v -> {
                    Bundle args = new Bundle();
                    if (eventId != null) {
                        args.putString("eventId", eventId);
                    }
                    Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_eventdetailsfragment, args);
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void readEventId() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        String eventIdValue = args.getString("eventId");
        if (eventIdValue == null) {
            Bundle data = args.getBundle("data");
            if (data != null) {
                eventIdValue = data.getString("eventId");
            }
        }
        eventId = eventIdValue;
    }
}
