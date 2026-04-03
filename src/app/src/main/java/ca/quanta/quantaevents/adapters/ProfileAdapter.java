package ca.quanta.quantaevents.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.ItemProfileCardBinding;
import ca.quanta.quantaevents.models.ExternalUser;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class ProfileAdapter<VH extends ProfileAdapter.ProfileViewHolder> extends RecyclerView.Adapter<VH> {




    public static abstract class ProfileViewHolder extends RecyclerView.ViewHolder {
        public interface Factory<VH extends ProfileViewHolder> {
            VH createNew(ViewGroup parent);
        }

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract public void bind(ProfileAdapter adapter, List<ExternalUser> profiles, int position);
    }

    private final List<ExternalUser> profiles;
    private final ProfileViewHolder.Factory<VH> viewFactory;


    public ProfileAdapter(List<ExternalUser> profiles, ProfileViewHolder.Factory<VH> viewFactory) {
        this.viewFactory = viewFactory;
        this.profiles = profiles;
    }

    @Override
    public int getItemCount() {
        return this.profiles.size();
    }

    @Override
    @NonNull
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewFactory.createNew(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(this, this.profiles, position);
    }
}
