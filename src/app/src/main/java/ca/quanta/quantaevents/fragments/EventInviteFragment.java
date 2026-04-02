package ca.quanta.quantaevents.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.w3c.dom.Text;

import java.util.Optional;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventInviteBinding;

/**
 * Fragment for the invite page of a private event
 */
public class EventInviteFragment extends Fragment {

    FragmentEventInviteBinding binding;

    UUID eventId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventInviteFragmentArgs args = EventInviteFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.backButton.setOnClickListener(v -> {
            if(isAdded()) {
                Navigation.findNavController(v).popBackStack();
            }
        });

        binding.searchButton.setOnClickListener(v -> {
            if(isAdded()) {
                String searchStr = String.valueOf(binding.searchInput.getText());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventInviteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}