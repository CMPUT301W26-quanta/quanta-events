package ca.quanta.quantaevents.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.WaitingListAdapter;
import ca.quanta.quantaevents.databinding.FragmentEventWaitlistBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;

public class EventWaitingListFragment extends Fragment {
    private FragmentEventWaitlistBinding binding;
    private EventViewModel eventViewModel;
    private WaitingListAdapter adapter;
    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;
    private UUID eventId;

    private static final String[] LIST_LABELS = {
            "Wait List", "Selected List", "Final List", "Cancelled List", "Rejected List"
    };
    private static final String[] LIST_VALUES = {
            "waitList", "selectedList", "finalList", "cancelledList", "rejectedList"
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to QR code and the subtitle to share an event qr code.
        // also sets the icon for the page
        infoStore.setTitle("Event Waiting List");
        infoStore.setSubtitle("View waiting list entrants");
        infoStore.setIconRes(R.drawable.material_symbols_group_outline);

        eventId = EventWaitingListFragmentArgs.fromBundle(getArguments()).getEventId();
        adapter = new WaitingListAdapter();
        binding.profilesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.profilesRecyclerView.setAdapter(adapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                LIST_LABELS
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerFilter.setAdapter(spinnerAdapter);
        binding.spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchList(LIST_VALUES[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            fetchList(LIST_VALUES[binding.spinnerFilter.getSelectedItemPosition()]);
        });
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    private void fetchList(String listType) {
        if (userId == null || deviceId == null || eventId == null) {
            Log.w("EventWaitingList", "fetchList aborted: userId=" + userId
                    + " deviceId=" + deviceId + " eventId=" + eventId);
            return;
        };
        Log.d("EventWaitingList", "fetchList: listType=" + listType + " eventId=" + eventId);
        eventViewModel.getWaitlist(userId, deviceId, eventId, listType)
                .addOnSuccessListener(users -> {
                    if (!isAdded() || binding == null) return;
                    Log.d("EventWaitingList", "fetchList success: count=" + users.size());
                    adapter.setUsers(users);
                })
                .addOnFailureListener(ex -> {
                    if (!isAdded() || binding == null) return;
                    Log.e("EventWaitingList", "fetchList failed", ex);
                    ToastManager.show(getContext(), "Failed to load list", Toast.LENGTH_SHORT);
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventWaitlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
