package ca.quanta.quantaevents.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.functions.FirebaseFunctionsException;

import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.Comment;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.CommentViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {

    List<Comment> list;

    private Fragment parentFragment;

    private CommentViewModel commentModel;

    private SessionStore sessionStore;

    UUID userId;
    UUID deviceId;
    UUID eventId;

    boolean isOrganizer;
    boolean isAdmin;

    public CommentAdapter(List<Comment> list, Fragment parentFragment, UUID eventId, boolean isOrganizer, boolean isAdmin) {
        this.list = list;
        this.parentFragment = parentFragment;
        this.eventId = eventId;

        this.commentModel = new ViewModelProvider(this.parentFragment.getActivity()).get(CommentViewModel.class);
        this.sessionStore = new ViewModelProvider(this.parentFragment.requireActivity()).get(SessionStore.class);

        this.userId = null;
        this.deviceId = null;

        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;

        sessionStore.observeSession(this.parentFragment.getViewLifecycleOwner(), (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;
        });

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment, parent, false);
        return new MyHolder(view);
    }

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

                        Toast.makeText(this.parentFragment.requireContext(), "Failed to remove comment: " + exception.getMessage(), Toast.LENGTH_LONG).show();

                        if (exception instanceof FirebaseFunctionsException) {
                            Log.e("CommentAdapter", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                        }
                    });
        });


    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    public void addComment(Comment comment){
        this.list.add(comment);
        this.notifyItemInserted(this.list.size()-1);

    }

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
