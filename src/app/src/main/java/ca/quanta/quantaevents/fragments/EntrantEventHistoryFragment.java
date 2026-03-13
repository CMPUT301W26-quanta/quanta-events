package ca.quanta.quantaevents.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.functions.FirebaseFunctionsException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.EventCardAdapter;
import ca.quanta.quantaevents.adapters.EventCardItem;
import ca.quanta.quantaevents.databinding.FragmentEntrantEventHistoryBinding;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class EntrantEventHistoryFragment extends Fragment {
    private FragmentEntrantEventHistoryBinding binding;
    private EventCardAdapter adapter;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("History");
        infoStore.setSubtitle("View enrolled event history");
        infoStore.setIconRes(R.drawable.material_symbols_history);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        adapter = new EventCardAdapter(item -> {
            NavDirections action = ca.quanta.quantaevents.fragments.EntrantEventHistoryFragmentDirections.actionEntrantEventHistoryFragmentToEventDetailsFragment(item.getEventId());
            Navigation.findNavController(requireView()).navigate(action);
        });
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventsRecyclerView.setAdapter(adapter);

        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            loadHistoryEvents();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEntrantEventHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void loadHistoryEvents() {
        if (userId == null || deviceId == null) {
            Log.d("EntrantEventHistory", "Session missing: userId/deviceId null");
            return;
        }
        Log.d("EntrantEventHistory", "Loading history events for user " + userId);
        eventModel.getEvents(
                        userId,
                        deviceId,
                        50,
                        null,
                        EventViewModel.Fetch.HISTORY,
                        null,
                        null,
                        null,
                        EventViewModel.SortBy.NAME)
                .addOnSuccessListener(this::bindEventList)
                .addOnFailureListener(ex -> {
                    Log.e("EntrantEventHistory", "Failed to load events", ex);
                    if (ex instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException fex = (FirebaseFunctionsException) ex;
                        Log.e("EntrantEventHistory", "Functions code=" + fex.getCode() + " message=" + fex.getMessage());
                    }
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                        return;
                    }
                    Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_LONG).show();
                });
    }

    private void bindEventList(List<Event> events) {
        if (events == null) {
            Log.d("EntrantEventHistory", "Event list is null");
            return;
        }
        Log.d("EntrantEventHistory", "Loaded events count=" + events.size());
        if (events.isEmpty()) {
            Toast.makeText(requireContext(), "No events found", Toast.LENGTH_LONG).show();
            adapter.setItems(new ArrayList<>());
            return;
        }
        ArrayList<EventCardItem> items = new ArrayList<>();
        for (Event event : events) {
            if (event == null) {
                continue;
            }
            UUID eventId = event.getEventId();
            String title = stringValue(event.getEventName(), "Event");
            String time = formatLocalTime(event.getRegistrationStartTime());
            String location = stringValue(event.getLocation(), "TBD");
            items.add(new EventCardItem(eventId, title, time, location, null));
        }
        adapter.setItems(items);
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event != null && event.getImageId() != null && i < items.size()) {
                fetchAndAttachImage(event.getEventId(), items.get(i), event.getImageId());
            }
        }
    }

    private void fetchAndAttachImage(UUID eventId, EventCardItem item, UUID imageId) {
        imageModel.getImage(imageId, userId, deviceId)
                .addOnSuccessListener(data -> {
                    Object imageData = data.get("imageData");
                    if (imageData == null) {
                        return;
                    }
                    Bitmap bitmap = decodeBase64ToBitmap(imageData.toString());
                    if (bitmap != null) {
                        adapter.upsert(item.withImage(bitmap));
                    }
                });
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String result = value.toString().trim();
        return result.isEmpty() ? fallback : result;
    }

    private String formatLocalTime(@Nullable ZonedDateTime value) {
        if (value == null) {
            return "TBD";
        }
        ZonedDateTime local = value.withZoneSameInstant(ZoneId.systemDefault());
        return local.format(displayFormatter);
    }

    private static Bitmap decodeBase64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isUserNotFound(Exception ex) {
        if (ex instanceof FirebaseFunctionsException) {
            FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) ex).getCode();
            return code == FirebaseFunctionsException.Code.NOT_FOUND;
        }
        return false;
    }

    private void handleMissingUser() {
        sessionStore.clearSession();
        if (isAdded()) {
            NavDirections action = ca.quanta.quantaevents.fragments.EntrantEventHistoryFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
    }
}
