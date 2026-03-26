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
import ca.quanta.quantaevents.adapters.ProfileAdapter;
import ca.quanta.quantaevents.databinding.FragmentAdminProfileBrowserBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.ExternalUser;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class AdminProfileBrowserFragment extends Fragment {
    private FragmentAdminProfileBrowserBinding binding;

    private UserViewModel userModel;

    private UUID userId;
    private UUID deviceId;
    private boolean hasLoaded = false;

    // lists all profiles to show to admin and adds them to the array
    private void listProfiles() {
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
            this.userModel.getAllUsers(this.userId, this.deviceId)
                    .addOnSuccessListener(users -> {
                        // filter out admins

                        ArrayList<ExternalUser> nonAdminProfiles = new ArrayList<ExternalUser>();

                        for (ExternalUser user : users) {
                            if (!user.isAdmin()) {
                                nonAdminProfiles.add(user);
                            }
                        }

                        // use the adapter to display them

                        ProfileAdapter profilesAdapter = new ProfileAdapter(nonAdminProfiles, this, () -> {
                            
                        });

                        binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                        binding.profilesRecyclerView.setAdapter(profilesAdapter);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e("AdminProfileBrowserFragment", "Failed to fetch all users.", exception);

                        ToastManager.show(requireContext(), "Failed to fetch users", Toast.LENGTH_LONG);
                        if (exception instanceof FirebaseFunctionsException) {
                            Log.e("AdminProfileBrowserFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                        }
                    })
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Event and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Admin Profile Browser");
        infoStore.setSubtitle("Browse and remove profiles.");
        infoStore.setIconRes(R.drawable.material_symbols_person_shield_outline);

        // set up the view model
        // **** set up the view models

        this.userModel = new ViewModelProvider(this.requireActivity()).get(UserViewModel.class);

        // **** set up the session store

        SessionStore sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);

        /**
         * set up the profiles recycler view
         */
        sessionStore.observeSession(getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;

            // set up the profiles recycler view once the userId and deviceId are ready
            if (!hasLoaded) {
                hasLoaded = true;
                listProfiles();
            }
        });

        // *set up the backbuttons on click listener

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        hasLoaded = false;
        binding = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminProfileBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
