package ca.quanta.quantaevents.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.ProfileAdapter;
import ca.quanta.quantaevents.databinding.FragmentEventInviteBinding;
import ca.quanta.quantaevents.databinding.ItemInviteProfileCardBinding;
import ca.quanta.quantaevents.databinding.ItemProfileCardBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.ExternalUser;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

/**
 * Fragment for the invite page of a private event
 */
public class EventInviteFragment extends Fragment {

    /**
     * View holder for holding profile views.
     */
    public static class InviteProfileViewHolder extends ProfileAdapter.ProfileViewHolder {

        /**
         * Factory class for creating profile view holders.
         */
        public static class Factory implements ProfileAdapter.ProfileViewHolder.Factory<InviteProfileViewHolder> {
            private final OnInviteButtonClickedListener listener;

            /**
             * Constructor for a factory.
             * @param listener Invite button click listener for the event.
             */
            public Factory( OnInviteButtonClickedListener listener) {
                this.listener = listener;
            }

            /**
             * Creates a new invite profile view holder.
             * @param parent The view group this belongs to.
             * @return New invite profile view holder.
             */
            @Override
            public InviteProfileViewHolder createNew(ViewGroup parent) {
                ItemInviteProfileCardBinding binding = ItemInviteProfileCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new InviteProfileViewHolder(binding.getRoot(), listener);
            }
        }

        /**
         * Interface for invite button functionality.
         */
        public interface OnInviteButtonClickedListener {
            void OnInviteButtonClickedListener(UUID profileID);
        }

        private final ItemInviteProfileCardBinding binding;
        private final OnInviteButtonClickedListener listener;

        /**
         * Constructor for an invite profile view holder.
         * @param itemView Root view of the view holder.
         * @param listener Invite button click listener for the event.
         */
        public InviteProfileViewHolder(@NonNull View itemView, OnInviteButtonClickedListener listener) {
            super(itemView);
            binding = ItemInviteProfileCardBinding.bind(itemView);
            this.listener = listener;
        }

        @Override
        public void bind(ProfileAdapter _adapter, List<ExternalUser> profiles, int position) {
            ExternalUser user = profiles.get(position);
            // Set name
            binding.profileName.setText(user.getName());

            // Setup add button
            binding.iconAdd.setOnClickListener(_view -> {
                listener.OnInviteButtonClickedListener(user.getUserId());
            });
        }
    }

    FragmentEventInviteBinding binding;

    UUID userId;
    UUID deviceId;
    UUID eventId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventInviteFragmentArgs args = EventInviteFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        EventViewModel events = provider.get(EventViewModel.class);
        UserViewModel users = provider.get(UserViewModel.class);
        SessionStore session = provider.get(SessionStore.class);
        LoaderState loader = provider.get(LoaderState.class);

        session.observeSession(this, (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;
        });

        binding.backButton.setOnClickListener(v -> {
            if(isAdded()) {
                Navigation.findNavController(v).popBackStack();
            }
        });

        ArrayList<ExternalUser> profiles = new ArrayList<>();
        ProfileAdapter<InviteProfileViewHolder> adapter = getInviteProfileViewHolderProfileAdapter(events, profiles);
        binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.profilesRecyclerView.setAdapter(adapter);

        binding.searchButton.setOnClickListener(v -> {
            if(isAdded()) {
                String searchStr = String.valueOf(binding.searchInput.getText());
                loader.loadTask(
                        users.getAllUsers(userId, deviceId, searchStr)
                                .addOnSuccessListener(matching -> {
                                    profiles.clear();
                                    profiles.addAll(matching);
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(exc -> {
                                    Log.e("FINDING USERS", exc.toString());
                                    if (isAdded()) ToastManager.show(getContext(), "Failed to load users", Toast.LENGTH_LONG);
                                })
                );
            }
        });


    }

    @NonNull
    private ProfileAdapter<InviteProfileViewHolder> getInviteProfileViewHolderProfileAdapter(EventViewModel events, ArrayList<ExternalUser> profiles) {
        InviteProfileViewHolder.Factory factory = new InviteProfileViewHolder.Factory(invitee -> {
            events.createInvitation(userId, deviceId, eventId, invitee)
                    .addOnSuccessListener(_void -> {
                        if(isAdded()) ToastManager.show(getContext(), "Sent invitation", Toast.LENGTH_SHORT);
                    })
                    .addOnFailureListener(exc -> {
                        Log.e("INVITING USER", exc.toString());
                        if(isAdded()) ToastManager.show(getContext(), "Failed to send invitation", Toast.LENGTH_LONG);
                    });
        });
        return new ProfileAdapter<>(profiles, factory);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventInviteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}