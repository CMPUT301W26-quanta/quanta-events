package ca.quanta.quantaevents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.ExternalNotification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final ArrayList<ExternalNotification> notifications;

    public NotificationAdapter(ArrayList<ExternalNotification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public int getItemCount() {
        return this.notifications.size();
    }

    @Override
    @NonNull
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_card, parent, false);

        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        ExternalNotification notification = notifications.get(position);

        String title = notification.getTitle();
        holder.title.setText(title != null ? title : "[Title not set]");

        String message = notification.getMessage();
        holder.message.setText(message != null ? message : "[Message not set]");
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView message;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            this.title = itemView.findViewById(R.id.notification_title);
            this.message = itemView.findViewById(R.id.notification_message);
        }
    }
}
