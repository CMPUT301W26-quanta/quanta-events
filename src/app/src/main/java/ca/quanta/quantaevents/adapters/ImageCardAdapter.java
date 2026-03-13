package ca.quanta.quantaevents.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ImageCardViewHolder> {
    private final ArrayList<Event> events;

    private UserViewModel userModel;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;

    private Fragment parentFragment;

    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;

    public ImageCardAdapter(List<Event> events, Fragment parentFragment) {
        this.parentFragment = parentFragment;

        this.userModel = new ViewModelProvider(this.parentFragment.getActivity()).get(UserViewModel.class);
        this.eventModel = new ViewModelProvider(this.parentFragment.getActivity()).get(EventViewModel.class);
        this.imageModel = new ViewModelProvider(this.parentFragment.getActivity()).get(ImageViewModel.class);
        this.events = new ArrayList<Event>();

        for (Event event : events) {
            UUID imageId = event.getImageId();

            // only add events that have images
            if (imageId != null) {
                this.events.add(event);
            }
        }

        // **** set up the session store

        this.sessionStore = new ViewModelProvider(this.parentFragment.requireActivity()).get(SessionStore.class);

        sessionStore.observeSession(this.parentFragment.getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    @NonNull
    public ImageCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_card, parent, false);

        return new ImageCardViewHolder(itemView);
    }

    private static Bitmap decodeBase64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException ex) {
            Log.e("ImageCardAdapter", "Failed to decode image.");
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ImageCardViewHolder holder, int position) {
        Event event = this.events.get(position);

        this.imageModel.getImage(event.getImageId(), this.userId, this.deviceId)
                .addOnSuccessListener(image -> {
                    Object imageData = image.get("imageData");

                    // put in a placeholder if the event has an image, but its image data is
                    // set to null
                    if (imageData == null) {
                        holder.image.setImageResource(R.drawable.material_symbols_image_rounded);
                    }
                    else {
                        Bitmap bitmap = decodeBase64ToBitmap(imageData.toString());

                        if (bitmap != null) {
                            holder.image.setImageBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(exception -> {
                    Log.e("ImageCardAdapter", "Failed to fetch an image.", exception);

                    Toast.makeText(this.parentFragment.requireContext(), "Failed to load image: " + exception.getMessage(), Toast.LENGTH_LONG).show();

                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("ImageCardAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });

        holder.buttonRemove.setOnClickListener(view -> {
            int eventPosition = holder.getBindingAdapterPosition();

            this.eventModel.updateEvent(
                    this.userId,
                    this.deviceId,
                    event.getEventId(),
                    event.getRegistrationStartTime().toString(),
                    event.getRegistrationEndTime().toString(),
                    event.getEventTime() == null ? null : event.getEventTime().toString(),
                    event.getEventName(),
                    event.getEventDescription(),
                    event.getEventCategory(),
                    event.getEventGuidelines(),
                    event.isGeolocationEnabled(),
                    event.getEventCapacity(),
                    event.getLocation(),
                    event.getRegistrationLimit(),
                    null
            )
                    .addOnSuccessListener(nil -> {
                        this.events.remove(eventPosition);
                        this.notifyItemRemoved(eventPosition);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e("ImageCardAdapter", "Failed to update an event.", exception);

                        Toast.makeText(this.parentFragment.requireContext(), "Failed to remove image: " + exception.getMessage(), Toast.LENGTH_LONG).show();

                        if (exception instanceof FirebaseFunctionsException) {
                            Log.e("ImageCardAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                        }
                    });
        });
    }

    public static class ImageCardViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        ImageView image;
        Button buttonRemove;

        public ImageCardViewHolder(@NonNull View itemView) {
            super(itemView);

            this.card = itemView.findViewById(R.id.image_card);
            this.image = itemView.findViewById(R.id.image_image_view);
            this.buttonRemove = itemView.findViewById(R.id.image_remove_button);
        }
    }
}
