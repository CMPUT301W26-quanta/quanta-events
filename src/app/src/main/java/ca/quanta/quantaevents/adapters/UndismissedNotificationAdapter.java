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
         * @param eventId  UUID of the event to fetch
         * @param consumer Consumes the fetched event
         * @param onFail   On failure to fetch the event, this is called instead
         */
        void getEvent(UUID eventId, Consumer<Event> consumer, Runnable onFail);

        /**
         * Dismisses the notification, optionally removing the notification if the behaviour requires it.
         * @param position       The position of this notification in the list.
         * @param notificationId UUID of this notification.
         */
        void dismissed(int position, UUID notificationId);

        /**
         * Accepts the invite with this given event id
         * @param eventId UUID of this event.
         */
        void accepted(UUID eventId);

        /**
         * Declines the invite with this given event id
         * @param eventId UUID of this event.
         */
        void declined(UUID eventId);

        /**
         * Accepts the co-organizer invite with this given event id
         * @param eventId
         */
        void acceptedCoInvite(UUID eventId);

        /**
         * Should navigate to the event details of the given event
         * @param eventId UUID of the event to navigate to.
         */
        void goToEvent(UUID eventId);

        /**
         * Sets which adapter to use.
         * @param adapter The adapter to use.
         */
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
         * @param notification The notification to bind
         */
        public abstract void bind(ExternalUndismissedNotification notification);
    }

    /**
     * An {@link ViewHolder} for COINVITE type {@link ExternalUndismissedNotification}
     */
    public static class CoInviteViewHolder extends ViewHolder {
        private final ItemLotteryNotificationCardBinding binding;
        private final AsyncHandler handler;

        private CoInviteViewHolder(ItemLotteryNotificationCardBinding binding, AsyncHandler handler) {
            super(binding.getRoot());
            this.binding = binding;
            this.handler = handler;
        }

        /**
         * Creates a co invite view holder.
         * @param parent View group this belongs to.
         * @param handler Handler to handle interaction with invite.
         * @return New co invite view holder.
         */
        public static CoInviteViewHolder newInstance(ViewGroup parent, AsyncHandler handler) {
            ItemLotteryNotificationCardBinding binding = ItemLotteryNotificationCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CoInviteViewHolder(binding, handler);
        }

        /**
         * Binds a notification and sets up co invite functionality.
         * @param notification The notification to bind
         */
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

            binding.iconDismiss.setVisibility(View.GONE);

            binding.notificationState.setText("Co-Organize this event?");

            binding.iconAccept.setOnClickListener(_v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION) {
                    handler.acceptedCoInvite(notification.getEventId());
                    handler.dismissed(position, notification.getNotificationId());
                }

            });
            binding.iconDecline.setOnClickListener(_v -> {
                int position = getBindingAdapterPosition();
                if (position != NO_POSITION)
                    handler.dismissed(position, notification.getNotificationId());
            });
        }
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

        /**
         * Creates a invite view holder.
         * @param parent View group this belongs to.
         * @param handler Handler to handle interaction with invite.
         * @return New invite view holder.
         */
        public static InviteViewHolder newInstance(ViewGroup parent, AsyncHandler handler) {
            ItemInviteNotificationCardBinding binding = ItemInviteNotificationCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new InviteViewHolder(binding, handler);
        }

        /**
         * Binds a notification and sets up invite functionality.
         * @param notification The notification to bind
         */
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

        /**
         * Creates a lottery view holder.
         * @param parent View group this belongs to.
         * @param handler Handler to handle interaction with invite.
         * @return New lottery view holder.
         */
        public static LotteryViewHolder newInstance(ViewGroup parent, AsyncHandler handler) {
            ItemLotteryNotificationCardBinding binding = ItemLotteryNotificationCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LotteryViewHolder(binding, handler);
        }

        /**
         * Binds a lottery notification and sets up functionality.
         * @param notification The notification to bind
         */
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

        /**
         * Creates a message view holder.
         * @param parent View group this belongs to.
         * @param handler Handler to handle interaction with message.
         * @return New message view holder.
         */
        public static MessageViewHolder newInstance(ViewGroup parent, AsyncHandler handler) {
            ItemMessageNotificationCardBinding binding = ItemMessageNotificationCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MessageViewHolder(binding, handler);
        }

        /**
         * Binds message notification and setups up functionality.
         * @param notification The notification to bind
         */
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

        /**
         * Creates a blank view holder.
         * @param parent View group this belongs to.
         * @return New blank view holder.
         */
        public static BlankViewHolder newInstance(ViewGroup parent) {
            View view = new View(parent.getContext());
            return new BlankViewHolder(view);
        }

        /**
         * Binds a notification.
         * @param notification The notification to bind
         */
        @Override
        public void bind(ExternalUndismissedNotification notification) {
        }
    }

    List<ExternalUndismissedNotification> notifications;
    AsyncHandler handler;

    /**
     * Creates a new {@link UndismissedNotificationAdapter}
     * @param notifications The list of notifications to adapt
     * @param handler       The async operation handler
     */
    public UndismissedNotificationAdapter(List<ExternalUndismissedNotification> notifications, AsyncHandler handler) {
        this.notifications = notifications;
        handler.setAdapter(this);
        this.handler = handler;
    }

    /**
     * Creates a new view holder based on view type.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return message, lottery, invite, co invite, or blank view holder based on view type.
     */
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
            case 3:
                return CoInviteViewHolder.newInstance(parent, handler);
            default:
                return BlankViewHolder.newInstance(parent);
        }
    }

    /**
     * Populates a view holder with notification data.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExternalUndismissedNotification notification = notifications.get(position);
        holder.bind(notification);
    }

    /**
     * Gets the number of notifications to be displayed.
     * @return Integer representing number of notifications to be displayed.
     */
    @Override
    public int getItemCount() {
        return this.notifications.size();
    }

    /**
     * Gets the view type of a notification.
     * @param position position to query
     * @return Integer 0, 1, 2, 3, or -1 based on notification type.
     */
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
            case "COINVITE":
                return 3;
            default:
                return -1;
        }
    }
}
