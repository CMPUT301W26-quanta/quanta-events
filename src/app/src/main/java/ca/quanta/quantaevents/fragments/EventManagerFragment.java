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
        infoStore.setTitle("Event Manager");
        infoStore.setSubtitle("Manage your Events");
        infoStore.setIconRes(R.drawable.material_symbols_edit_outline);

        readEventId();

        binding.editDetailsButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToCreateeditorfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        binding.shareQrButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToShowqrfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        binding.viewWaitListButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToEventwaitinglistfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
        binding.sendNotificationButton.setOnClickListener(
                v -> {
                    NavDirections action = EventManagerFragmentDirections.actionEventmanagerfragmentToSendnotificationfragment(eventId);
                    Navigation.findNavController(v).navigate(action);
                }
        );
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
