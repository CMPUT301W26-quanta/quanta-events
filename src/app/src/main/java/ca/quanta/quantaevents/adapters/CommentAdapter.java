package ca.quanta.quantaevents.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {

    Context context;
    List<Comment> list;

    UUID userId;

    UUID eventId;

    public CommentAdapter(Context context, List<Comment> list, UUID userId, UUID eventId) {
        this.context = context;
        this.list = list;
        this.userId = userId;
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment, parent, false);
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

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.name.setText(senderName);
        holder.time.setText(timedate);
        holder.comment.setText(message);
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView name, comment, time;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.comment_name);
            comment = itemView.findViewById(R.id.comment_text);
            time = itemView.findViewById(R.id.comment_time);
        }
    }
}
