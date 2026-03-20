package ca.quanta.quantaevents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;

public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.EventViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(EventCardItem item);
    }

    private final List<EventCardItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public EventCardAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventCardItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.time.setText(item.getTime());
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
     * No parameters needed
     * @return Returns size of the list of events
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
     * clear the existing list and return a new list of events to the fragment
     * @param newItems
     */
    public void setItems(List<EventCardItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final CardView card;

        final ShapeableImageView image;
        final TextView title;
        final TextView time;
        final TextView location;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);

            this.card = itemView.findViewById(R.id.event_card);
            this.image = itemView.findViewById(R.id.event_image);
            this.title = itemView.findViewById(R.id.event_title);
            this.time = itemView.findViewById(R.id.event_time);
            this.location = itemView.findViewById(R.id.event_location);
        }
    }
}
