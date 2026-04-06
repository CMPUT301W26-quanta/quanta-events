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

/**
 * Adapter for handling user profile cards.
 * @param <VH> Profile view holder type used by the adapter.
 */
public class ProfileAdapter<VH extends ProfileAdapter.ProfileViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * View holder for displaying user profiles.
     */
    public static abstract class ProfileViewHolder extends RecyclerView.ViewHolder {

        /**
         * Interface for creating profile view holders.
         * @param <VH> Type of view holder that the factory creates.
         */
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

    /**
     * Constructor for a profile adapter.
     * @param profiles List of user profiles to be displayed.
     * @param viewFactory Factory for creating view holders.
     */
    public ProfileAdapter(List<ExternalUser> profiles, ProfileViewHolder.Factory<VH> viewFactory) {
        this.viewFactory = viewFactory;
        this.profiles = profiles;
    }

    /**
     * Gets the number of profiles to display.
     * @return Integer representing number of profiles.
     */
    @Override
    public int getItemCount() {
        return this.profiles.size();
    }

    /**
     * Clears and replaces a list of profiles with all profiles.
     * @param profiles List to be replaced.
     */
    public void setProfiles(List<ExternalUser> profiles) {
        this.profiles.clear();
        this.profiles.addAll(profiles);
        notifyDataSetChanged();
    }

    /**
     * Gets all user profiles to be displayed.
     * @return List of external users containing profile info.
     */
    public List<ExternalUser> getProfiles() {
        return profiles;
    }

    /**
     * Inflates profile card layout.
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return View holder holding an inflated profile card view.
     */
    @Override
    @NonNull
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewFactory.createNew(parent);
    }

    /**
     * Populates a view holder with profile data.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(this, this.profiles, position);
    }
}
