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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.User;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private final List<User> profiles;

    private UserViewModel model;

    private Fragment parentFragment;

    public ProfileAdapter(List<User> profiles, Fragment parentFragment) {
        this.parentFragment = parentFragment;

        this.model = new ViewModelProvider(this.parentFragment.getActivity()).get(UserViewModel.class);
        this.profiles = profiles;
    }

    @Override
    public int getItemCount() {
        return profiles.size();
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
        User user = profiles.get(position);

        String name = user.getName();
        holder.name.setText(name != null ? name : "[null username]");

        holder.buttonIconClose.setOnClickListener(view -> {
            int profilePosition = holder.getBindingAdapterPosition();

            if (profilePosition == RecyclerView.NO_POSITION) {
                return;
            }

            UUID profileUserId = profiles.get(profilePosition).getUserId();

            SessionStore sessionStore = new ViewModelProvider(this.parentFragment.getActivity()).get(SessionStore.class);

            sessionStore.observeSession(this.parentFragment.getViewLifecycleOwner(), (userUUID, deviceUUID) -> {
                if (userUUID == null) {
                    Log.e("ProfileAdapter", "Failed to deleteUser because userUUID is NULL.");
                    return;
                }

                if (deviceUUID == null) {
                    Log.e("ProfileAdapter", "Failed to deleteUser because userUUID is NULL.");
                    return;
                }

                model.deleteUser(userUUID, deviceUUID, user.getUserId())
                        .addOnSuccessListener(v -> {
                            int pos = -1;

                            // I do this, instead of doing profiles.get(profilePosition), in case
                            // something has changed recently (bc this is async). eg another
                            // profile being removed in the meantime
                            for (int i = 0; i < profiles.size(); i++) {
                                User profile = profiles.get(i);
                                if (profile.getUserId() == profileUserId) {
                                    pos = i;
                                    break;
                                }
                            }

                            // sometimes this is called twice for some reason, so the profile
                            // has already been deleted successfully
                            // bc of that, we get some erroneous calls of this, and this gets
                            // output even tho everything went well
                            if (pos == -1) {
                                Log.e("ProfileAdapter", "Cannot find profile to delete.");
                                return;
                            }

                            this.profiles.remove(pos);
                            this.notifyItemRemoved(pos);
                        })
                        .addOnFailureListener(exception -> {
                            Toast.makeText(this.parentFragment.requireContext(), "Failed to deleteUser: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                           Log.e("ProfileAdapter", "Failed to deleteUser.");
                        });
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
                Navigation.findNavController(view).navigate(R.id.action_adminprofilebrowserFragment_to_adminNotificationHistoryFragment);
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
