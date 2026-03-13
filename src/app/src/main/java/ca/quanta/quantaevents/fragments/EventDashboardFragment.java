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
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.EventCardAdapter;
import ca.quanta.quantaevents.adapters.EventCardItem;
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentEventDashboardBinding;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class EventDashboardFragment extends Fragment implements Tagged {
    private FragmentEventDashboardBinding binding;
    private EventCardAdapter adapter;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private boolean loadedInitialEvents = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Dashboard");
        infoStore.setSubtitle("View events you have created");
        infoStore.setIconRes(R.drawable.material_symbols_dashboard_outline);
        binding.createButton.setOnClickListener(
                v -> {
                    NavDirections action = ca.quanta.quantaevents.fragments.EventDashboardFragmentDirections.actionEventdashboardFragmentToEventEditorFragment(null);
                    Navigation.findNavController(v).navigate(action);
                }
        );

        adapter = new EventCardAdapter(item -> {
            NavDirections action = ca.quanta.quantaevents.fragments.EventDashboardFragmentDirections.actionEventdashboardfragmentToEventedetailsfragment(item.getEventId());
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
            maybeLoadAllEvents();
        });

        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (userId != null && deviceId != null) {
            loadCreatedEvents();
        }
    }

    private void maybeLoadAllEvents() {
        if (loadedInitialEvents) {
            return;
        }
        if (userId == null || deviceId == null) {
            return;
        }
        loadedInitialEvents = true;
        loadCreatedEvents();
    }

    private void loadCreatedEvents() {
        eventModel.getEvents(
                        userId,
                        deviceId,
                        50,
                        null,
                        EventViewModel.Fetch.CREATED,
                        null,
                        null,
                        null,
                        EventViewModel.SortBy.REGISTRATION_END)
                .addOnSuccessListener(this::bindEventList)
                .addOnFailureListener(ex -> {
                    Log.e("EventDashboard", "Failed to load events", ex);
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                        return;
                    }
                    Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_LONG).show();
                });
    }

    private void bindEventList(java.util.List<Event> events) {
        if (events == null) {
            return;
        }
        if (events.isEmpty()) {
            Toast.makeText(requireContext(), "No events found", Toast.LENGTH_LONG).show();
            adapter.setItems(new java.util.ArrayList<>());
            return;
        }
        java.util.ArrayList<EventCardItem> items = new java.util.ArrayList<>();
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
                        adapter.updateInsert(item.withImage(bitmap));
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
            NavDirections action = ca.quanta.quantaevents.fragments.EventDashboardFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

    private final static UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }
}
