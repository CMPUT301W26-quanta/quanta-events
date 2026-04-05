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
import ca.quanta.quantaevents.viewmodels.InviteViewModel;
import ca.quanta.quantaevents.viewmodels.NotificationViewModel;

public class LotteryNotificationAdapter extends RecyclerView.Adapter<LotteryNotificationAdapter.NotificationViewHolder> {
    private final ArrayList<ExternalUndismissedNotification> notifications;
    HomeFragment parentFragment;
    EventViewModel eventModel;
    NotificationViewModel notificationModel;
    InviteViewModel inviteModel;
    private UUID userId;
    private UUID deviceId;

    public LotteryNotificationAdapter(ArrayList<ExternalUndismissedNotification> notifications, HomeFragment parentFragment, EventViewModel eventModel, NotificationViewModel notificationModel, InviteViewModel inviteModel, UUID userId, UUID deviceId) {
        this.notifications = notifications;
        this.parentFragment = parentFragment;

        // **** set up the view models

        this.eventModel = eventModel;
        this.notificationModel = notificationModel;
        this.inviteModel = inviteModel;

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
                .inflate(R.layout.item_lottery_notification_card, parent, false);

        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        ExternalUndismissedNotification notification = notifications.get(position);

        this.eventModel.getEvent(notification.getEventId(), this.userId, this.deviceId)
                .addOnSuccessListener(event -> {
                    if (notification.getLotterySelected())
                        holder.message.setText(String.format("Join event: %s", event.getEventName()));
                    else
                        holder.message.setText(String.format("Lost lottery for: %s", event.getEventName()));
                })
                .addOnFailureListener(exception -> {
                    holder.message.setText(notification.getLotterySelected() ? "Join event." : "Lost lottery for event.");
                    Log.e("LotteryNotificationAdapter", "Failed to fetch event name.", exception);

                    ToastManager.show(this.parentFragment.getContext(), "Failed to fetch event name.", Toast.LENGTH_LONG);

                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("LotteryNotificationAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });

        if (notification.getLotterySelected()) {
            holder.buttonDismiss.setVisibility(View.GONE);

            holder.buttonAccept.setOnClickListener(v -> {
                inviteModel.inviteAccept(this.userId, this.deviceId, notification.getEventId())
                        .addOnSuccessListener(x -> {
                            ToastManager.show(this.parentFragment.getContext(), "Successfully accepted invitation.", Toast.LENGTH_LONG);
                        })
                        .addOnFailureListener(exception -> {
                            Log.e("LotteryNotificationAdapter", "Failed to accept invite..", exception);

                            ToastManager.show(this.parentFragment.getContext(), "Failed to accept invite.", Toast.LENGTH_LONG);

                            if (exception instanceof FirebaseFunctionsException) {
                                Log.e("LotteryNotificationAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                            }
                        });

                dismissNotification(holder.getBindingAdapterPosition(), notification.getNotificationId());
            });

            holder.buttonDecline.setOnClickListener(v -> {
                inviteModel.inviteReject(this.userId, this.deviceId, notification.getEventId())
                        .addOnSuccessListener(x -> {
                            ToastManager.show(this.parentFragment.getContext(), "Successfully rejected invitation.", Toast.LENGTH_LONG);
                        })
                        .addOnFailureListener(exception -> {
                            Log.e("LotteryNotificationAdapter", "Failed to reject invite..", exception);

                            ToastManager.show(this.parentFragment.getContext(), "Failed to reject invite.", Toast.LENGTH_LONG);

                            if (exception instanceof FirebaseFunctionsException) {
                                Log.e("LotteryNotificationAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                            }
                        });

                dismissNotification(holder.getBindingAdapterPosition(), notification.getNotificationId());
            });
        }
        else {
            holder.buttonAccept.setVisibility(View.GONE);
            holder.buttonDecline.setVisibility(View.GONE);

            holder.buttonDismiss.setOnClickListener(v -> {
                dismissNotification(holder.getBindingAdapterPosition(), notification.getNotificationId());
            });
        }

        holder.buttonEvent.setOnClickListener(v -> {
            NavDirections action = ca.quanta.quantaevents.fragments.HomeFragmentDirections.actionHomeFragmentToEventDetailsFragment(notification.getEventId());
            Navigation.findNavController(this.parentFragment.requireView()).navigate(action);
        });
    }

    private void dismissNotification(int notificationPosition, UUID notificationId) {
        // optimistically remove the notification
        this.notifications.remove(notificationPosition);
        this.notifyItemRemoved(notificationPosition);

        this.notificationModel.dismissNotification(this.userId, this.deviceId, notificationId)
                .addOnFailureListener(exception -> {
                    Log.e("LotteryNotificationAdapter", "Failed to dismiss notification.", exception);

                    ToastManager.show(this.parentFragment.getContext(), "Failed to dismiss notification.", Toast.LENGTH_LONG);

                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("LotteryNotificationAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView buttonAccept;
        ImageView buttonDecline;
        ImageView buttonDismiss;
        ImageView buttonEvent;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            this.message = itemView.findViewById(R.id.notification_message);
            this.buttonAccept = itemView.findViewById(R.id.icon_accept);
            this.buttonDecline = itemView.findViewById(R.id.icon_decline);
            this.buttonDismiss = itemView.findViewById(R.id.icon_dismiss);
            this.buttonEvent = itemView.findViewById(R.id.icon_event);
        }
    }
}
