package ca.quanta.quantaevents.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.ExternalUser;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    public interface OnNotificationsButtonClickedListener {
        void onNotificationsButtonClicked(UUID profileID);
    }

    private final List<ExternalUser> profiles;

    private UserViewModel model;

    private Fragment parentFragment;

    private SessionStore sessionStore;

    private UUID userId;
    private UUID deviceId;
    private final OnNotificationsButtonClickedListener onNotificationsButtonClickedListener;

    public ProfileAdapter(List<ExternalUser> profiles, Fragment parentFragment, OnNotificationsButtonClickedListener onNotificationsButtonClickedListener) {
        this.parentFragment = parentFragment;

        this.model = new ViewModelProvider(this.parentFragment.getActivity()).get(UserViewModel.class);
        this.profiles = profiles;

        this.onNotificationsButtonClickedListener = onNotificationsButtonClickedListener;

        // **** set up the session store

        this.sessionStore = new ViewModelProvider(this.parentFragment.getActivity()).get(SessionStore.class);

        this.userId = null;
        this.deviceId = null;

        this.sessionStore.observeSession(this.parentFragment.getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;
        });
    }

    @Override
    public int getItemCount() {
        return this.profiles.size();
    }

    @Override
    @NonNull
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_card, parent, false);

        return new ProfileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        ExternalUser user = this.profiles.get(position);

        String name = user.getName();
        holder.name.setText(name != null ? name : "[null username]");

        holder.buttonIconClose.setOnClickListener(view -> {
            int profilePosition = holder.getBindingAdapterPosition();

            if (profilePosition == RecyclerView.NO_POSITION) {
                return;
            }

            if (this.userId == null || this.deviceId == null) {
                Log.e("ProfileAdapter", "Failed to deleteUser because userId or deviceId is NULL.");
                Toast.makeText(this.parentFragment.requireContext(), "Still loading user. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            model.deleteUser(this.userId, this.deviceId, user.getUserId())
                    .addOnSuccessListener(v -> {
                        this.profiles.remove(profilePosition);
                        this.notifyItemRemoved(profilePosition);
                    })
                    .addOnFailureListener(exception -> {
                        Toast.makeText(this.parentFragment.requireContext(), "Failed to deleteUser: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("ProfileAdapter", "Failed to deleteUser.");
                    });
        });

        if (!user.isOrganizer()) {
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.card_background_default)
            );

            // not an organizer, remove the notifications button
            holder.buttonIconNotifications.setVisibility(View.GONE);
        } else {
            // is an organizer, set a different background colour to highlight this fact
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.card_background_organizer)
            );

            // add a click listener to the notification button

            holder.buttonIconNotifications.setOnClickListener(view -> {
                this.onNotificationsButtonClickedListener.onNotificationsButtonClicked(user.getUserId());
            });
        }
    }

    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView name;
        ImageView buttonIconNotifications;
        ImageView buttonIconClose;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);

            this.card = itemView.findViewById(R.id.profile_card);
            this.name = itemView.findViewById(R.id.profile_name);
            this.buttonIconNotifications = itemView.findViewById(R.id.icon_notifications);
            this.buttonIconClose = itemView.findViewById(R.id.icon_close);
        }
    }
}
