package ca.quanta.quantaevents.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;

/**
 * Adapter for handling event cards.
 */
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.EventViewHolder> {

    /**
     * Interface of item click listener.
     */
    public interface OnItemClickListener {
        void onItemClick(EventCardItem item);
    }

    private final List<EventCardItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    /**
     * Constructor for this object.
     * @param listener Event card click listener.
     */
    public EventCardAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Inflates event card layout.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return View holder holding an inflated event card view.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Sets the values for an event card.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventCardItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.eventStartTime.setText(item.getRegistrationStartTime());
        holder.location.setText(item.getLocation());

        // set the event card's background colour
        holder.card.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.card_background_default)
        );

        if (item.getImage() != null) {
            holder.image.setImageBitmap(item.getImage());
        } else {
            holder.image.setImageResource(R.drawable.material_symbols_image_rounded);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    /**
     * Gets the size of the list of events.
     * @return Integer size of the list of events
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * checks if event exists in list if it does update it else add another (similar to upsert in sql databases)
     * @param item
     * No return value
     */
    public void updateInsert(EventCardItem item) {
        for (int i = 0; i < items.size(); i++) {
            EventCardItem current = items.get(i);
            UUID currentId = current.getEventId();
            if (currentId != null && currentId.equals(item.getEventId())) {
                items.set(i, item);
                notifyItemChanged(i);
                return;
            }
        }
        items.add(0, item);
        notifyItemInserted(0);
    }

    /**
     * Updates the location of an event.
     * @param eventId UUID identifying the event.
     * @param location String identifying the event location.
     */
    public void updateLocation(UUID eventId, String location) {
        for (int i = 0; i < items.size(); i++) {
            EventCardItem current = items.get(i);
            if (current.getEventId() != null && current.getEventId().equals(eventId)) {
                items.set(i, new EventCardItem(
                        current.getEventId(),
                        current.getTitle(),
                        current.getRegistrationStartTime(),
                        location,
                        current.getImage()
                ));
                notifyItemChanged(i);
                return;
            }
        }
    }

    /**
     * Updates the event image.
     * @param eventId UUID identifying the event.
     * @param image Bitmap containing new image data.
     */
    public void updateImage(UUID eventId, Bitmap image) {
        for (int i = 0; i < items.size(); i++) {
            EventCardItem current = items.get(i);
            if (current.getEventId() != null && current.getEventId().equals(eventId)) {
                items.set(i, new EventCardItem(
                        current.getEventId(),
                        current.getTitle(),
                        current.getRegistrationStartTime(),
                        current.getLocation(),  // preserves location
                        image
                ));
                notifyItemChanged(i);
                return;
            }
        }
    }

    /**
     * clears the existing list and make it a new list of events to the fragment
     * @param newItems List of events to be cleared and replaced.
     */
    public void setItems(List<EventCardItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    /**
     * View holder for displaying an event card.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        final CardView card;

        final ShapeableImageView image;
        final TextView title;
        final TextView eventStartTime;
        final TextView location;

        /**
         * Constructor for this class.
         * @param itemView Inflated event card view.
         */
        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            this.card = itemView.findViewById(R.id.event_card);
            this.image = itemView.findViewById(R.id.event_image);
            this.title = itemView.findViewById(R.id.event_title);
            this.eventStartTime = itemView.findViewById(R.id.event_start_time);
            this.location = itemView.findViewById(R.id.event_location);
        }
    }
}
