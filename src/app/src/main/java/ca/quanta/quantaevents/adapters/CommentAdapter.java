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
import ca.quanta.quantaevents.models.ModelComment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {

    Context context;
    List<ModelComment> list;

    UUID userId;

    UUID eventId;

    public CommentAdapter(Context context, List<ModelComment> list, UUID userId, UUID eventId) {
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
        final UUID userId = list.get(position).getUserId();
        String userName  = list.get(position).getUserName();
        final UUID commentId = list.get(position).getCommentId();
        String comment = list.get(position).getComment();
        String timestamp = list.get(position).getPostTime();
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String timedate = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        holder.name.setText(userName);
        holder.time.setText(timedate);
        holder.comment.setText(comment);

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
