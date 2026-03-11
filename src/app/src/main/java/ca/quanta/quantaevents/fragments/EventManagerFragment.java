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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Manager");
        infoStore.setSubtitle("Manage your Events");
        infoStore.setIconRes(R.drawable.material_symbols_edit_outline);

        binding.editDetailsButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_eventdetailsfragment)
        );
        binding.shareQrButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_showqrfragment)
        );
        binding.viewWaitListButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_eventwaitinglistfragment)
        );
        binding.sendNotificationButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_sendnotificationfragment)
        );
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventmanagerfragment_to_eventdetailsfragment)
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventManagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
