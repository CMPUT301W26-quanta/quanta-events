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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.List;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.ProfileAdapter;
import ca.quanta.quantaevents.databinding.FragmentAdminProfileBrowserBinding;
import ca.quanta.quantaevents.models.User;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class AdminProfileBrowserFragment extends Fragment {
    private FragmentAdminProfileBrowserBinding binding;

    private UserViewModel model;

    private void listProfiles() {
        model.getAllUsers()
                .addOnSuccessListener(users -> {
                    // filter out admins

                    ArrayList<User> nonAdminProfiles = new ArrayList<User>();

                    for (User user : users) {
                        if (!user.isAdmin()) {
                            nonAdminProfiles.add(user);
                        }
                    }

                    // use the adapter to display them

                    ProfileAdapter profilesAdapter = new ProfileAdapter(nonAdminProfiles);

                    binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                    binding.profilesRecyclerView.setAdapter(profilesAdapter);
                })
                .addOnFailureListener(exception -> {
                    Log.e("AdminProfileBrowserFragment", "Failed to fetch all users.", exception);

                    Toast.makeText(requireContext(), "Failed to fetch users: " + exception.getMessage(), Toast.LENGTH_LONG).show();

                    // added during debugging, but keeping it bc its useful
                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("AdminProfileBrowserFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });;

//        // TODO: fetch all profiles from the database instead
//
//        List<User> profiles = new ArrayList<User>();
//
//        profiles.add(new User("Robert Smith", null, null, null, false, true, false));
//        profiles.add(new User("Christian Vasquez", null, null, null, false, true, false));
//        profiles.add(new User("Sandra Thomas", null, null, null, false, false, false));
//        profiles.add(new User("Some Admin", null, null, null, false, false, true));
//
//        // filter out admins
//
//        List<User> nonAdminProfiles = new ArrayList<User>();
//
//        for (User profile : profiles) {
//            if (!profile.isAdmin()) {
//                nonAdminProfiles.add(profile);
//            }
//        }
//
//        // use the adapter to display them
//
//        ProfileAdapter profilesAdapter = new ProfileAdapter(nonAdminProfiles);
//
//        binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
//        binding.profilesRecyclerView.setAdapter(profilesAdapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // **** set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);

        infoStore.setTitle("Admin Profile Browser");
        infoStore.setSubtitle("Browse and remove profiles.");
        infoStore.setIconRes(R.drawable.material_symbols_person_shield_outline);

        // **** set up the view model

        this.model = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        // **** set up the profiles recycler view

        this.listProfiles();

        // **** set up the buttons

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminProfileBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
