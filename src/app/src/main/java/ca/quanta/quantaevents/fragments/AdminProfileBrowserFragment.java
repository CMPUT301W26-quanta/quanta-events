package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.ProfileAdapter;
import ca.quanta.quantaevents.databinding.FragmentAdminProfileBrowserBinding;
import ca.quanta.quantaevents.databinding.ItemProfileCardBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.ExternalUser;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class AdminProfileBrowserFragment extends Fragment {
    public static class AdminProfileViewHolder extends ProfileAdapter.ProfileViewHolder {
        public static class Factory implements ProfileAdapter.ProfileViewHolder.Factory<AdminProfileViewHolder> {
            private final UUID userId;
            private final UUID deviceId;
            UserViewModel model;
            private final OnNotificationsButtonClickedListener listener;
            public Factory(UUID userId, UUID deviceId, UserViewModel model, OnNotificationsButtonClickedListener listener) {
                this.userId = userId;
                this.deviceId = deviceId;
                this.listener = listener;
                this.model = model;
            }

            @Override
            public AdminProfileViewHolder createNew(ViewGroup parent) {
                ItemProfileCardBinding binding = ItemProfileCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new AdminProfileViewHolder(binding.getRoot(), userId, deviceId, model, listener);
            }
        }

        public interface OnNotificationsButtonClickedListener {
            void onNotificationsButtonClicked(UUID profileID);
        }

        private final ItemProfileCardBinding binding;
        private final UUID userId;
        private final UUID deviceId;
        private final UserViewModel model;
        private final OnNotificationsButtonClickedListener listener;


        public AdminProfileViewHolder(@NonNull View itemView, UUID userId, UUID deviceId, UserViewModel model, OnNotificationsButtonClickedListener listener) {
            super(itemView);
            binding = ItemProfileCardBinding.bind(itemView);
            this.userId = userId;
            this.deviceId = deviceId;
            this.listener = listener;
            this.model = model;
        }

        @Override
        public void bind(ProfileAdapter adapter, List<ExternalUser> profiles, int position) {
            ExternalUser user = profiles.get(position);
            // Set name
            binding.profileName.setText(user.getName());

            binding.iconEdit.setOnClickListener(view -> {
                int profilePosition = this.getBindingAdapterPosition();

                if (profilePosition == RecyclerView.NO_POSITION) {
                    return;
                }

                if (this.userId == null || this.deviceId == null) {
                    Toast.makeText(binding.getRoot().getContext(), "Still loading user. Please try again.", Toast.LENGTH_LONG).show();
                    return;
                }

                // Navigate to new screen
                UUID targetUserId = user.getUserId();
                NavDirections action = ca.quanta.quantaevents.fragments.AdminProfileBrowserFragmentDirections.actionAdminprofilebrowserFragmentToAdminAccountEditFragment(targetUserId);
                Navigation.findNavController(view).navigate(action);
            });

            // Setup close button
            binding.iconClose.setOnClickListener(view -> {
                int profilePosition = this.getBindingAdapterPosition();

                if (profilePosition == RecyclerView.NO_POSITION) {
                    return;
                }

                if (this.userId == null || this.deviceId == null) {
                    Log.e("ProfileAdapter", "Failed to deleteUser because userId or deviceId is NULL.");
                    Toast.makeText(binding.getRoot().getContext(), "Still loading user. Please try again.", Toast.LENGTH_LONG).show();
                    return;
                }

                model.deleteUser(this.userId, this.deviceId, user.getUserId())
                        .addOnSuccessListener(v -> {
                            profiles.remove(profilePosition);
                            adapter.notifyItemRemoved(profilePosition);
                        })
                        .addOnFailureListener(exception -> {
                            Toast.makeText(binding.getRoot().getContext(), "Failed to deleteUser: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("ProfileAdapter", "Failed to deleteUser.");
                        });
            });

            this.binding.profileCard.setCardBackgroundColor(
                    ContextCompat.getColor(this.binding.getRoot().getContext(), R.color.card_background_default)
            );

            // remove the notifications button by default
            this.binding.iconNotifications.setVisibility(View.GONE);

            if (user.isOrganizer()) {
                // is an organizer, set a different background colour to highlight this fact
                this.binding.profileCard.setCardBackgroundColor(
                        ContextCompat.getColor(this.binding.getRoot().getContext(), R.color.card_background_organizer)
                );

                // make the notifications button visible

                this.binding.iconNotifications.setVisibility(View.VISIBLE);

                // add a click listener to the notification button

                this.binding.iconNotifications.setOnClickListener(view -> {
                    this.listener.onNotificationsButtonClicked(user.getUserId());
                });
            }

            if (user.isAdmin()) {
                this.binding.iconEdit.setVisibility(View.GONE);
                this.binding.iconClose.setVisibility(View.GONE);
                this.binding.profileCard.setCardBackgroundColor(
                        ContextCompat.getColor(this.binding.getRoot().getContext(), R.color.card_background_admin)
                );
            }
        }
    }

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
                            if (!this.isAdded() || this.binding == null) return;

                            // use the adapter to display them

                            ProfileAdapter<AdminProfileViewHolder> profilesAdapter = getAdminProfileViewHolderProfileAdapter(users);

                            this.binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                            this.binding.profilesRecyclerView.setAdapter(profilesAdapter);
                        })
                        .addOnFailureListener(exception -> {
                            if (!this.isAdded() || this.binding == null) return;
                            Log.e("AdminProfileBrowserFragment", "Failed to fetch all users.", exception);

                            ToastManager.show(getContext(), "Failed to fetch users", Toast.LENGTH_LONG);
                            if (exception instanceof FirebaseFunctionsException) {
                                Log.e("AdminProfileBrowserFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                            }
                        })
        );
    }

    @NonNull
    private ProfileAdapter<AdminProfileViewHolder> getAdminProfileViewHolderProfileAdapter(ArrayList<ExternalUser> nonAdminProfiles) {
        AdminProfileViewHolder.Factory factory = new AdminProfileViewHolder.Factory(userId, deviceId, userModel, (profileID) -> {
            NavDirections action = ca.quanta.quantaevents.fragments.AdminProfileBrowserFragmentDirections.actionAdminprofilebrowserFragmentToAdminNotificationHistoryFragment(profileID);
            Navigation.findNavController(this.requireView()).navigate(action);
        });
        ProfileAdapter<AdminProfileViewHolder> profilesAdapter = new ProfileAdapter(nonAdminProfiles, factory);
        return profilesAdapter;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Event and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Admin Profile Browser");
        infoStore.setSubtitle("Browse and remove profiles.");
        infoStore.setIconRes(R.drawable.material_symbols_person_shield_outline);

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

        // *set up the back buttons on click listener

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
