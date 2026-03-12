package ca.quanta.quantaevents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void upsert(EventCardItem item) {
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

    static class EventViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView image;
        final TextView title;
        final TextView time;
        final TextView location;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.event_image);
            title = itemView.findViewById(R.id.event_title);
            time = itemView.findViewById(R.id.event_time);
            location = itemView.findViewById(R.id.event_location);
        }
    }
}
