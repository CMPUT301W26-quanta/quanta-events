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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.ProfileAdapter;
import ca.quanta.quantaevents.databinding.FragmentAdminProfileBrowserBinding;
import ca.quanta.quantaevents.models.User;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class AdminProfileBrowserFragment extends Fragment {
    private FragmentAdminProfileBrowserBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // **** set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);

        infoStore.setTitle("Admin Profile Browser");
        infoStore.setSubtitle("Browse and Delete Profiles");
        infoStore.setIconRes(R.drawable.material_symbols_person_shield_outline);

        // **** set up the profiles recycler view

        // TODO: fetch all profiles from the database instead

        List<User> profiles = new ArrayList<User>();

        profiles.add(new User("Robert Smith", null, null, null, false, true, false));
        profiles.add(new User("Christian Vasquez", null, null, null, false, true, false));
        profiles.add(new User("Sandra Thomas", null, null, null, false, false, false));
        profiles.add(new User("Some Admin", null, null, null, false, false, true));

        // filter out admins

        List<User> nonAdminProfiles = new ArrayList<User>();

        for (User profile : profiles) {
            if (!profile.isAdmin()) {
                nonAdminProfiles.add(profile);
            }
        }

        ProfileAdapter profilesAdapter = new ProfileAdapter(nonAdminProfiles);

        binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.profilesRecyclerView.setAdapter(profilesAdapter);

        // **** set up the buttons

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_adminprofilebrowserFragment_to_adminpanelFragment)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminProfileBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
