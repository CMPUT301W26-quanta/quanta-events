package ca.quanta.quantaevents.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.List;

import ca.quanta.quantaevents.databinding.ItemWaitingListUserBinding;
import ca.quanta.quantaevents.models.ExternalUser;

public class WaitingListViewHolder extends ProfileAdapter.ProfileViewHolder {
    private static final String TAG = "WaitlistUserAdapter";
    private final ItemWaitingListUserBinding binding;

    private WaitingListViewHolder(@NonNull ItemWaitingListUserBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public static final Factory<WaitingListViewHolder> FACTORY = parent -> {
        ItemWaitingListUserBinding b = ItemWaitingListUserBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WaitingListViewHolder(b);
    };

    @Override
    public void bind(ProfileAdapter adapter, List<ExternalUser> profiles, int position) {
        ExternalUser user = profiles.get(position);
        Log.d(TAG, "bind: position=" + position + " name=" + user.getName()
                + " email=" + user.getEmail() + " phone=" + user.getPhone());
        binding.entrantName.setText(user.getName());
        binding.entrantEmail.setText(user.getEmail());
        binding.entrantPhone.setText(user.getPhone());
    }
}