package ca.quanta.quantaevents.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.fragments.HomeFragment;
import ca.quanta.quantaevents.models.ExternalUndismissedNotification;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.NotificationViewModel;

public class InviteNotificationAdapter extends RecyclerView.Adapter<InviteNotificationAdapter.NotificationViewHolder> {
    private final ArrayList<ExternalUndismissedNotification> notifications;
    HomeFragment parentFragment;
    EventViewModel eventModel;
    NotificationViewModel notificationModel;
    private UUID userId;
    private UUID deviceId;

    public InviteNotificationAdapter(ArrayList<ExternalUndismissedNotification> notifications, HomeFragment parentFragment, EventViewModel eventModel, NotificationViewModel notificationModel, UUID userId, UUID deviceId) {
        this.notifications = notifications;
        this.parentFragment = parentFragment;

        // **** set up the view models

        this.eventModel = eventModel;
        this.notificationModel = notificationModel;

        // **** set up the session store

        this.userId = userId;
        this.deviceId = deviceId;
    }

    @Override
    public int getItemCount() {
        return this.notifications.size();
    }

    @Override
    @NonNull
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invite_notification_card, parent, false);

        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        ExternalUndismissedNotification notification = notifications.get(position);

        eventModel.getEvent(notification.getEventId(), this.userId, this.deviceId)
                .addOnSuccessListener(event -> {
                    holder.message.setText(String.format("Invited to private event: %s", event.getEventName()));
                })
                .addOnFailureListener(exception -> {
                    holder.message.setText("Invited to private event.");
                    Log.e("InviteNotificationAdapter", "Failed to fetch event name.", exception);

                    ToastManager.show(this.parentFragment.getContext(), "Failed to fetch event name.", Toast.LENGTH_LONG);

                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("InviteNotificationAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });

        holder.buttonEvent.setOnClickListener(v -> {
            NavDirections action = ca.quanta.quantaevents.fragments.HomeFragmentDirections.actionHomeFragmentToEventDetailsFragment(notification.getEventId());
            Navigation.findNavController(this.parentFragment.requireView()).navigate(action);
        });

        holder.buttonDismiss.setOnClickListener(v -> {
            int notificationPosition = holder.getBindingAdapterPosition();

            // optimistically remove the notification
            this.notifications.remove(notificationPosition);
            this.notifyItemRemoved(notificationPosition);

            this.notificationModel.dismissNotification(this.userId, this.deviceId, notification.getNotificationId())
                    .addOnFailureListener(exception -> {
                        Log.e("InviteNotificationAdapter", "Failed to dismiss notification.", exception);

                        ToastManager.show(this.parentFragment.getContext(), "Failed to dismiss notification.", Toast.LENGTH_LONG);

                        if (exception instanceof FirebaseFunctionsException) {
                            Log.e("InviteNotificationAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                        }
                    });
        });
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView buttonEvent;
        ImageView buttonDismiss;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            this.message = itemView.findViewById(R.id.notification_message);
            this.buttonEvent = itemView.findViewById(R.id.icon_event);
            this.buttonDismiss = itemView.findViewById(R.id.icon_dismiss);
        }
    }
}
