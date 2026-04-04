package ca.quanta.quantaevents.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentInviteCoOrganizerBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;

public class InviteCoOrganizerFragment extends Fragment {

    private UUID userId;
    private UUID deviceId;
    private SessionStore sessionStore;
    private FragmentInviteCoOrganizerBinding binding;

    public InviteCoOrganizerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentInviteCoOrganizerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // sets title as account
        // sets subtitle and the icon
        infoStore.setTitle("Invite");
        infoStore.setSubtitle("Add other users as co-organizers");
        infoStore.setIconRes(R.drawable.material_symbols_person_outline);

        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        // verify and check session
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
        });

        InviteCoOrganizerFragmentArgs args = InviteCoOrganizerFragmentArgs.fromBundle(this.getArguments());

        binding.backButton.setOnClickListener(
                v -> {
                    Navigation.findNavController(v).popBackStack();
                }
        );
    }

}