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

/**
 * Adapter for handling notification cards.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final ArrayList<ExternalNotification> notifications;

    /**
     * Constructor for a notification adapter.
     * @param notifications ArrayList containing notifications to be displayed.
     */
    public NotificationAdapter(ArrayList<ExternalNotification> notifications) {
        this.notifications = notifications;
    }

    /**
     * Gets the number of notifications to be displayed.
     * @return Integer representing the number of notifications to be displayed.
     */
    @Override
    public int getItemCount() {
        return this.notifications.size();
    }

    /**
     * Inflates the notification card layout.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return View holder holding an inflated notification view.
     */
    @Override
    @NonNull
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_card, parent, false);

        return new NotificationViewHolder(itemView);
    }

    /**
     * Sets notification card display details.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        ExternalNotification notification = notifications.get(position);

        String title = notification.getTitle();
        holder.title.setText(title != null ? title : "[Title not set]");

        String message = notification.getMessage();
        holder.message.setText(message != null ? message : "[Message not set]");
    }

    /**
     * View holder for displaying a notification card.
     */
    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView message;

        /**
         * Constructor for a notification view holder.
         * @param itemView Inflated notification card view.
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            this.title = itemView.findViewById(R.id.notification_title);
            this.message = itemView.findViewById(R.id.notification_message);
        }
    }
}
