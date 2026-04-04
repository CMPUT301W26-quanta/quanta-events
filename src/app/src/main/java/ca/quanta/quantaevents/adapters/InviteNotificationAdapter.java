package ca.quanta.quantaevents.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.ExternalUndismissedNotification;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.NotificationViewModel;

public class InviteNotificationAdapter extends RecyclerView.Adapter<InviteNotificationAdapter.NotificationViewHolder> {
    private final ArrayList<ExternalUndismissedNotification> notifications;
    EventViewModel eventModel;
    NotificationViewModel notificationModel;
    private UUID userId;
    private UUID deviceId;
    Fragment parentFragment;

    public InviteNotificationAdapter(ArrayList<ExternalUndismissedNotification> notifications, Fragment parentFragment) {
        this.notifications = notifications;
        this.parentFragment = parentFragment;

        // **** set up the view models

        this.eventModel = new ViewModelProvider(parentFragment.requireActivity()).get(EventViewModel.class);
        this.notificationModel = new ViewModelProvider(parentFragment.requireActivity()).get(NotificationViewModel.class);

        // **** set up the session store

        SessionStore sessionStore = new ViewModelProvider(parentFragment.requireActivity()).get(SessionStore.class);

        sessionStore.observeSession(parentFragment.getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;
        });
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

            notificationModel.dismissNotification(this.userId, this.deviceId, notification.getNotificationId())
                    .addOnSuccessListener(x -> {
                        this.notifications.remove(notificationPosition);
                        this.notifyItemRemoved(notificationPosition);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e("InviteNotificationAdapter", "Failed to dismiss notification.", exception);

                        ToastManager.show(parentFragment.getContext(), "Failed to dismiss notification.", Toast.LENGTH_LONG);

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
