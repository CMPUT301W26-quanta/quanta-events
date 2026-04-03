package ca.quanta.quantaevents.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Optional;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventInviteBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

/**
 * Fragment for the invite page of a private event
 */
public class EventInviteFragment extends Fragment {

    FragmentEventInviteBinding binding;

    UUID userId;
    UUID deviceId;
    UUID eventId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventInviteFragmentArgs args = EventInviteFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        EventViewModel events = provider.get(EventViewModel.class);
        UserViewModel users = provider.get(UserViewModel.class);
        SessionStore session = provider.get(SessionStore.class);
        LoaderState loader = provider.get(LoaderState.class);

        session.observeSession(this, (userId, deviceId) -> {
            this.userId = userId;
            this.deviceId = deviceId;
        });

        binding.backButton.setOnClickListener(v -> {
            if(isAdded()) {
                Navigation.findNavController(v).popBackStack();
            }
        });

        binding.searchButton.setOnClickListener(v -> {
            if(isAdded()) {
                String searchStr = String.valueOf(binding.searchInput.getText());
                loader.loadTask(
                        users.getAllUsers(userId, deviceId, searchStr)
                                .addOnSuccessListener(matching -> {
                                })
                                .addOnFailureListener(exc -> {
                                    Log.e("FINDING USERS", exc.toString());
                                    if (isAdded()) ToastManager.show(getContext(), "Failed to load users", Toast.LENGTH_LONG);
                                })
                );
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventInviteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}