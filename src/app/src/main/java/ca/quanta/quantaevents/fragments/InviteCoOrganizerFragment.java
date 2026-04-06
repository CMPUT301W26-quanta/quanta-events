package ca.quanta.quantaevents.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.ProfileAdapter;
import ca.quanta.quantaevents.databinding.FragmentInviteCoOrganizerBinding;
import ca.quanta.quantaevents.databinding.ItemCoProfileCardBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.ExternalUser;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class InviteCoOrganizerFragment extends Fragment {

    public static class OrganizerProfileViewHolder extends ProfileAdapter.ProfileViewHolder {
        public static class Factory implements ProfileAdapter.ProfileViewHolder.Factory<InviteCoOrganizerFragment.OrganizerProfileViewHolder> {
            private final UUID userId;
            private final UUID deviceId;
            UserViewModel model;
            private final InviteCoOrganizerFragment.OrganizerProfileViewHolder.OnInviteButtonClickedListener listener;
            public Factory(UUID userId, UUID deviceId, UserViewModel model, InviteCoOrganizerFragment.OrganizerProfileViewHolder.OnInviteButtonClickedListener listener) {
                this.userId = userId;
                this.deviceId = deviceId;
                this.listener = listener;
                this.model = model;
            }

            @Override
            public InviteCoOrganizerFragment.OrganizerProfileViewHolder createNew(ViewGroup parent) {
                ItemCoProfileCardBinding binding = ItemCoProfileCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new InviteCoOrganizerFragment.OrganizerProfileViewHolder(binding.getRoot(), userId, deviceId, model, listener);
            }
        }

        public interface OnInviteButtonClickedListener {
            void onInviteButtonClicked(UUID profileID);
        }

        private final ItemCoProfileCardBinding binding;
        private final UUID userId;
        private final UUID deviceId;
        private final UserViewModel model;
        private final InviteCoOrganizerFragment.OrganizerProfileViewHolder.OnInviteButtonClickedListener listener;


        public OrganizerProfileViewHolder(@NonNull View itemView, UUID userId, UUID deviceId, UserViewModel model, InviteCoOrganizerFragment.OrganizerProfileViewHolder.OnInviteButtonClickedListener listener) {
            super(itemView);
            binding = ItemCoProfileCardBinding.bind(itemView);
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

            // Send an invite to be a co-organizer
            binding.iconAdd.setOnClickListener(view -> {
                this.listener.onInviteButtonClicked(user.getUserId());
            });

            if (!user.isOrganizer()) {
                binding.profileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.card_background_default)
                );
            } else {
                // is an organizer, set a different background colour to highlight this fact
                binding.profileCard.setCardBackgroundColor(
                        ContextCompat.getColor(binding.getRoot().getContext(), R.color.card_background_organizer)
                );
            }
        }
    }

    private UUID userId;
    private UUID deviceId;
    private SessionStore sessionStore;
    private UserViewModel userModel;
    private EventViewModel eventModel;
    private boolean hasLoaded = false;
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
        this.userModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        this.eventModel = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        // verify and check session
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            if (!hasLoaded) {
                hasLoaded = true;
                listProfiles();
            }
        });

        binding.backButton.setOnClickListener(
                v -> {
                    Navigation.findNavController(v).popBackStack();
                }
        );
    }

    /**
     * Lists all profiles to show to organizer and adds them to an array.
     */
    private void listProfiles() {
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
                this.userModel.getAllUsers(this.userId, this.deviceId)
                        .addOnSuccessListener(users -> {

                            if (!isAdded() || binding == null) return;

                            // filter out non-entrants
                            ArrayList<ExternalUser> entrantProfiles = new ArrayList<ExternalUser>();
                            for (ExternalUser user : users) {
                                if (user.isEntrant() && !user.getUserId().equals(this.userId)) {
                                    entrantProfiles.add(user);
                                }
                            }

                            // use the adapter to display them
                            ProfileAdapter<InviteCoOrganizerFragment.OrganizerProfileViewHolder> profilesAdapter = getAOrganizerProfileViewHolderProfileAdapter(entrantProfiles);

                            binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                            binding.profilesRecyclerView.setAdapter(profilesAdapter);
                        })
                        .addOnFailureListener(exception -> {
                            if (!isAdded() || binding == null) return;
                            Log.e("InviteCoOrganizerFragment", "Failed to fetch all users.", exception);

                            ToastManager.show(getContext(), "Failed to fetch users", Toast.LENGTH_LONG);
                            if (exception instanceof FirebaseFunctionsException) {
                                Log.e("InviteCoOrganizerFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                            }
                        })
        );
    }

    @NonNull
    private ProfileAdapter<InviteCoOrganizerFragment.OrganizerProfileViewHolder> getAOrganizerProfileViewHolderProfileAdapter(ArrayList<ExternalUser> entrantProfiles) {
        InviteCoOrganizerFragment.OrganizerProfileViewHolder.Factory factory = new InviteCoOrganizerFragment.OrganizerProfileViewHolder.Factory(userId, deviceId, userModel, (profileID) -> {

            // Send an invitation to be a co organizer
            InviteCoOrganizerFragmentArgs args = InviteCoOrganizerFragmentArgs.fromBundle(this.getArguments());
            eventModel.createCoInvitation(this.userId, this.deviceId, args.getEventId(), profileID)
                    .addOnSuccessListener(v -> {
                                if (!isAdded() || binding == null) return;
                                ToastManager.show(getContext(), "Invite Sent", Toast.LENGTH_LONG);
                            }
                    )
                    .addOnFailureListener(v -> {
                        if (!isAdded() || binding == null) return;
                        ToastManager.show(getContext(), "Failed to send invite", Toast.LENGTH_LONG);
                    });

        });
        ProfileAdapter<InviteCoOrganizerFragment.OrganizerProfileViewHolder> profilesAdapter = new ProfileAdapter(entrantProfiles, factory);
        return profilesAdapter;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        hasLoaded = false;
        binding = null;
    }

}