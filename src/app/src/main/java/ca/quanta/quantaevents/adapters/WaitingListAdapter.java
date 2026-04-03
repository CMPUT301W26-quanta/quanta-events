package ca.quanta.quantaevents.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.models.ExternalUser;

public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {
    private static final String TAG = "WaitlistUserAdapter";
    private List<ExternalUser> users = new ArrayList<>();

    public void setUsers(List<ExternalUser> users) {
        Log.d(TAG, "setUsers: count=" + (users == null ? "null" : users.size()));
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waiting_list_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExternalUser user = users.get(position);
        Log.d(TAG, "onBindViewHolder: position=" + position + " name=" + user.getName()
                + " email=" + user.getEmail() + " phone=" + user.getPhone());
        holder.name.setText(user.getName());
        holder.email.setText(user.getEmail());
        holder.phone.setText(user.getPhone());
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + users.size());
        return users.size();
    }

    public List<ExternalUser> getUsers() {
        return users;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, phone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.entrant_name);
            email = itemView.findViewById(R.id.entrant_email);
            phone = itemView.findViewById(R.id.entrant_phone);
        }
    }
}