package ca.quanta.quantaevents.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.ArrayList;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class ImageCardAdapter extends RecyclerView.Adapter<ImageCardAdapter.ImageCardViewHolder> {
    private final ArrayList<UUID> imageIDs;

    private ImageViewModel imageModel;

    private Fragment parentFragment;

    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;

    public ImageCardAdapter(ArrayList<UUID> imageIDs, Fragment parentFragment) {
        this.parentFragment = parentFragment;

        this.imageModel = new ViewModelProvider(this.parentFragment.getActivity()).get(ImageViewModel.class);
        this.imageIDs = imageIDs;

        // **** set up the session store

        this.sessionStore = new ViewModelProvider(this.parentFragment.requireActivity()).get(SessionStore.class);

        this.userId = null;
        this.deviceId = null;

        sessionStore.observeSession(this.parentFragment.getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;
        });
    }

    @Override
    public int getItemCount() {
        return imageIDs.size();
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
        UUID imageId = this.imageIDs.get(position);

        if (this.userId == null || this.deviceId == null) {
            Log.e("ImageCardAdapter", "Failed to bind image; userId or deviceId is NULL.");
            Toast.makeText(this.parentFragment.requireContext(), "Still loading user. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }

        // reset the image resource because the view might have been recycled
        holder.image.setImageResource(R.drawable.material_symbols_image_rounded);

        this.imageModel.getImage(imageId, this.userId, this.deviceId)
                .addOnSuccessListener(image -> {
                    Object imageData = image.getImageData();

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

            // optimistically remove the image
            this.imageIDs.remove(eventPosition);
            this.notifyItemRemoved(eventPosition);

            this.imageModel.deleteImage(imageId, this.userId, this.deviceId)
                    .addOnFailureListener(exception -> {
                        Log.e("ImageCardAdapter", "Failed to delete the image.", exception);

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
        ImageButton buttonRemove;

        public ImageCardViewHolder(@NonNull View itemView) {
            super(itemView);

            this.card = itemView.findViewById(R.id.image_card);
            this.image = itemView.findViewById(R.id.image_image_view);
            this.buttonRemove = itemView.findViewById(R.id.image_remove_button);
        }
    }
}
