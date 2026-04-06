package ca.quanta.quantaevents.adapters;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import ca.quanta.quantaevents.databinding.ItemInviteNotificationCardBinding;
import ca.quanta.quantaevents.databinding.ItemLotteryNotificationCardBinding;
import ca.quanta.quantaevents.databinding.ItemMessageNotificationCardBinding;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.models.ExternalUndismissedNotification;

/**
 * A {@link RecyclerView.Adapter} of undismissed notifications
 */
public class UndismissedNotificationAdapter extends RecyclerView.Adapter<UndismissedNotificationAdapter.ViewHolder> {

    /**
     * The interface of any type that handles the async operations of this adapter
     */
    public interface AsyncHandler {
        /**
         * Retrieve the requested event and provide the success or failure to the callbacks.
         * <p>
         * SAFETY: Must handle lifetime/existence checks to be safe. No checks are performed internally.
         *
         * @param eventId  UUID of the event to fetch
         * @param consumer Consumes the fetched event
         * @param onFail   On failure to fetch the event, this is called instead
         */
        void getEvent(UUID eventId, Consumer<Event> consumer, Runnable onFail);

        /**
         * Dismisses the notification, optionally removing the notification if the behaviour requires it.
         *
         * @param position       The position of this notification in the list.
         * @param notificationId UUID of this notification.
         */
        void dismissed(int position, UUID notificationId);

        /**
         * Accepts the invite with this given notification id
         *
         * @param eventId UUID of this event.
         */
        void accepted(UUID eventId);

        /**
         * Declines the invite with this given notification id
         *
         * @param eventId UUID of this event.
         */
        void declined(UUID eventId);

        /**
         * Should navigate to the event details of the given event
         *
         * @param eventId UUID of the event to navigate to.
         */
        void goToEvent(UUID eventId);

        void setAdapter(UndismissedNotificationAdapter adapter);
    }

    /**
     * Abstract view holder for binding {@link ExternalUndismissedNotification}s
     */
    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View itemView) {
            super(itemView);
        }

        /**
         * Bind the notification at the current position to this view holder
         *
         * @param notification The notification to bind
         * @param position     Position of this notification
         */
        public abstract void bind(ExternalUndismissedNotification notification);
    }

    /**
     * An {@link ViewHolder} for INVITE type {@link ExternalUndismissedNotification}
     */
    public static class InviteViewHolder extends ViewHolder {
        private final ItemInviteNotificationCardBinding binding;
        private final AsyncHandler handler;

        private InviteViewHolder(ItemInviteNotificationCardBinding binding, AsyncHandler handler) {
            super(binding.getRoot());
            this.binding = binding;
            this.handler = handler;
        }

        public static InviteViewHolder newInstance(ViewGroup parent, AsyncHandler handler) {
            ItemInviteNotificationCardBinding binding = ItemInviteNotificationCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new InviteViewHolder(binding, handler);
        }

        @Override
        public void bind(ExternalUndismissedNotification notification) {
            handler.getEvent(
                    notification.getEventId(),
                    event -> {
                        binding.notificationEventName.setText(event.getEventName());
                    },
                    () -> {
                        binding.notificationEventName.setText("Unknown Event Name");
                    }
            );

            binding.iconDismiss.setOnClickListener(_v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION)
                    handler.dismissed(position, notification.getNotificationId());
            });
            binding.iconEvent.setOnClickListener(_v -> handler.goToEvent(notification.getEventId()));
        }
    }

    /**
     * An {@link ViewHolder} for LOTTERY type {@link ExternalUndismissedNotification}
     */
    public static class LotteryViewHolder extends ViewHolder {
        private final ItemLotteryNotificationCardBinding binding;
        private final AsyncHandler handler;

        private LotteryViewHolder(ItemLotteryNotificationCardBinding binding, AsyncHandler handler) {
            super(binding.getRoot());
            this.binding = binding;
            this.handler = handler;
        }

        public static LotteryViewHolder newInstance(ViewGroup parent, AsyncHandler handler) {
            ItemLotteryNotificationCardBinding binding = ItemLotteryNotificationCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LotteryViewHolder(binding, handler);
        }

        @Override
        public void bind(ExternalUndismissedNotification notification) {
            handler.getEvent(
                    notification.getEventId(),
                    event -> {
                        binding.notificationEventName.setText(event.getEventName());
                    },
                    () -> {
                        binding.notificationEventName.setText("Unknown Event Name");
                    }
            );

            if (notification.getLotterySelected()) {
                binding.iconDismiss.setVisibility(View.GONE);

                binding.notificationState.setText("Join this event?");

                binding.iconAccept.setOnClickListener(_v -> {
                    int position = getBindingAdapterPosition();
                    if (position != NO_POSITION) {
                        handler.accepted(notification.getEventId());
                        handler.dismissed(position, notification.getNotificationId());
                    }

                });
                binding.iconDecline.setOnClickListener(_v -> {
                    int position = getBindingAdapterPosition();
                    if (position != NO_POSITION) {
                        handler.declined(notification.getEventId());
                        handler.dismissed(position, notification.getNotificationId());
                    }
                });
            } else {
                binding.iconAccept.setVisibility(View.GONE);
                binding.iconDecline.setVisibility(View.GONE);

                binding.notificationState.setText("Lottery lost...");

                binding.iconDismiss.setOnClickListener(_v -> {
                    int position = getBindingAdapterPosition();
                    if (position != NO_POSITION)
                        handler.dismissed(position, notification.getNotificationId());
                });
            }

            binding.iconEvent.setOnClickListener(_v -> handler.goToEvent(notification.getEventId()));
        }
    }

    /**
     * An {@link ViewHolder} for MESSAGE type {@link ExternalUndismissedNotification}
     */
    public static class MessageViewHolder extends ViewHolder {
        private final ItemMessageNotificationCardBinding binding;
        private final AsyncHandler handler;

        private MessageViewHolder(ItemMessageNotificationCardBinding binding, AsyncHandler handler) {
            super(binding.getRoot());
            this.binding = binding;
            this.handler = handler;
        }

        public static MessageViewHolder newInstance(ViewGroup parent, AsyncHandler handler) {
            ItemMessageNotificationCardBinding binding = ItemMessageNotificationCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MessageViewHolder(binding, handler);
        }

        @Override
        public void bind(ExternalUndismissedNotification notification) {
            binding.notificationTitle.setText(notification.getTitle());
            binding.notificationMessage.setText(notification.getMessage());
            handler.getEvent(
                    notification.getEventId(),
                    event -> {
                        binding.notificationEventName.setText(event.getEventName());
                    },
                    () -> {
                        binding.notificationEventName.setText("Unknown Event Name");
                    });
            binding.iconDismiss.setOnClickListener(_v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION)
                    handler.dismissed(position, notification.getNotificationId());
            });
            binding.iconEvent.setOnClickListener(_v -> handler.goToEvent(notification.getEventId()));
        }
    }

    /**
     * A blank {@link ViewHolder} for unknown notification types
     */
    public static class BlankViewHolder extends ViewHolder {
        private BlankViewHolder(View itemView) {
            super(itemView);
        }

        public static BlankViewHolder newInstance(ViewGroup parent) {
            View view = new View(parent.getContext());
            return new BlankViewHolder(view);
        }

        @Override
        public void bind(ExternalUndismissedNotification notification) {
        }
    }

    List<ExternalUndismissedNotification> notifications;
    AsyncHandler handler;

    /**
     * Creates a new {@link UndismissedNotificationAdapter}
     *
     * @param notifications The list of notifications to adapt
     * @param handler       The async operation handler
     */
    public UndismissedNotificationAdapter(List<ExternalUndismissedNotification> notifications, AsyncHandler handler) {
        this.notifications = notifications;
        handler.setAdapter(this);
        this.handler = handler;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return MessageViewHolder.newInstance(parent, handler);
            case 1:
                return LotteryViewHolder.newInstance(parent, handler);
            case 2:
                return InviteViewHolder.newInstance(parent, handler);
            default:
                return BlankViewHolder.newInstance(parent);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExternalUndismissedNotification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return this.notifications.size();
    }

    @Override
    public int getItemViewType(int position) {
        ExternalUndismissedNotification notification = notifications.get(position);
        switch (notification.getKind()) {
            case "MESSAGE":
                return 0;
            case "LOTTERY":
                return 1;
            case "INVITE":
                return 2;
            default:
                return -1;
        }
    }
}
