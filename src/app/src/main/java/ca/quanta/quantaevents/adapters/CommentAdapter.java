package ca.quanta.quantaevents.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.fragments.EventDetailsFragment;
import ca.quanta.quantaevents.models.Comment;
import ca.quanta.quantaevents.viewmodels.CommentViewModel;

/**
 * Adapter for handling comment cards.
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {
    List<Comment> list;

    private EventDetailsFragment parentFragment;
    private CommentViewModel commentModel;

    private UUID userId;
    private UUID deviceId;
    private UUID eventId;

    private boolean isOrganizer;
    private boolean isAdmin;

    /**
     * Constructor for this class.
     * @param list List of comment objects.
     * @param parentFragment Fragment which displays comments.
     * @param commentModel Comment object.
     * @param eventId UUID identifying event the comments are for.
     * @param userId UUID identifying user who wrote comment.
     * @param deviceId UUID identifying user's device.
     * @param isOrganizer Boolean that's true if the user is an organizer.
     * @param isAdmin Boolean that's true if the user is an admin.
     */
    public CommentAdapter(
            List<Comment> list,
            EventDetailsFragment parentFragment,
            CommentViewModel commentModel,
            UUID eventId,
            UUID userId,
            UUID deviceId,
            boolean isOrganizer,
            boolean isAdmin
    ) {
        this.list = list;
        this.parentFragment = parentFragment;
        this.commentModel = commentModel;

        this.eventId = eventId;
        this.userId = userId;
        this.deviceId = deviceId;

        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
    }

    /**
     * Inflates comment card layout.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return View holder holding an inflated comment view.
     */
    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false);
        return new MyHolder(view);
    }

    /**
     * Sets the values for a comment cards and handles their deletion.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final Comment thisComment = list.get(position);

        final UUID commentId = thisComment.getCommentId();
        final UUID senderId = thisComment.getSenderId();
        final String senderName  = thisComment.getSenderName();
        final String message = thisComment.getMessage();
        final String timestamp = thisComment.getPostTime();

        if (this.userId != senderId && !this.isOrganizer && !this.isAdmin) {
            // not the organizer of the event, an admin, or the user who wrote the comment;
            // remove the delete button
            holder.deleteComment.setVisibility(View.GONE);
        }

        holder.name.setText(senderName);
        holder.time.setText(timestamp);
        holder.comment.setText(message);

        holder.deleteComment.setOnClickListener(view -> {
            int commentPosition = holder.getBindingAdapterPosition();

            this.commentModel.deleteComment(this.userId, this.deviceId, this.eventId, commentId)
                    .addOnSuccessListener(success -> {
                        this.list.remove(commentPosition);
                        this.notifyItemRemoved(commentPosition);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e("CommentAdapter", "Failed to delete an comment.", exception);

                        Toast.makeText(this.parentFragment.getContext(), "Failed to remove comment: " + exception.getMessage(), Toast.LENGTH_LONG).show();

                        if (exception instanceof FirebaseFunctionsException) {
                            Log.e("CommentAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                        }
                    });
        });
    }

    /**
     * Gets the size of the comment list.
     * @return Integer size of the list.
     */
    @Override
    public int getItemCount(){
        return list.size();
    }

    /**
     * Add a comment to the comment list.
     * @param comment Comment to be added.
     */
    public void addComment(Comment comment){
        this.list.add(comment);
        this.notifyItemInserted(this.list.size()-1);
    }

    /**
     * View holder for displaying a comment card.
     */
    class MyHolder extends RecyclerView.ViewHolder {

        TextView name, comment, time;

        MaterialButton deleteComment;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.comment_name);
            comment = itemView.findViewById(R.id.comment_text);
            time = itemView.findViewById(R.id.comment_time);
            deleteComment = itemView.findViewById(R.id.delete_comment);
        }
    }
}
