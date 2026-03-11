package ca.quanta.quantaevents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.User;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private final List<User> profiles;

    public ProfileAdapter(List<User> profiles) {
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
        holder.name.setText(name != null ? name : "[NO USER.NAME]");

        holder.buttonIconClose.setOnClickListener(view -> {
            int profilePosition = holder.getBindingAdapterPosition();

            profiles.remove(profilePosition);
        });

        if (!user.isOrganizer()) {
            // not an organizer, remove the notifications button
            holder.buttonIconNotifications.setVisibility(View.GONE);
        } else {
            // is an organizer, set a different background colour to highlight this fact
            holder.card.setCardBackgroundColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.organizer_highlight)
            );
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
