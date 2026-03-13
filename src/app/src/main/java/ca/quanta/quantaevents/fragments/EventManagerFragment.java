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

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventManagerBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventManagerFragment extends Fragment {
    private FragmentEventManagerBinding binding;
    private UUID eventId;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Event Manaher and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Event Manager");
        infoStore.setSubtitle("Manage your Events");
        infoStore.setIconRes(R.drawable.material_symbols_edit_outline);

        // reads event id passed as an argument in the bundle
        readEventId();

        // sets on click listener to navigate to editor fragment
        binding.editDetailsButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToCreateeditorfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        // sets onc lick listener to navigate to show qr fragment
        binding.shareQrButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToShowqrfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        //sets on click listener to navigate to waiting list fragment
        binding.viewWaitListButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToEventwaitinglistfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        // set on click listner to navigate to send notification fragment
        binding.sendNotificationButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToSendnotificationfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        // send on click listener to navigate back to previous fragment
        binding.backButton.setOnClickListener(
                v -> {
                    Navigation.findNavController(v).popBackStack();
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
        EventManagerFragmentArgs args = EventManagerFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
    }
}
