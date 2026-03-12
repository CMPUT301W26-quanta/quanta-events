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
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class EventDashboardFragment extends Fragment implements Tagged {
    private FragmentEventDashboardBinding binding;
    private EventCardAdapter adapter;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private UserViewModel userModel;
    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;
    private UUID pendingEventId;
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
                v -> Navigation.findNavController(v).navigate(R.id.action_eventdashboardfragment_to_eventeditorfragment)
        );

        adapter = new EventCardAdapter(item -> {
            Bundle args = new Bundle();
            if (item.getEventId() != null) {
                args.putString("eventId", item.getEventId().toString());
            }
            Navigation.findNavController(requireView())
                    .navigate(R.id.eventDetailsFragment, args);
        });
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventsRecyclerView.setAdapter(adapter);

        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        userModel = new ViewModelProvider(this).get(UserViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            maybeFetchPending();
            maybeLoadAllEvents();
        });
        handleIncomingEventId();

        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }


    private void handleIncomingEventId() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }
        String eventIdValue = args.getString("eventId");
        Log.d("EventDashboard", "Incoming eventId arg: " + eventIdValue);
        if (eventIdValue == null || eventIdValue.isEmpty()) {
            return;
        }
        UUID eventId = parseUUID(eventIdValue);
        if (eventId == null) {
            return;
        }
        pendingEventId = eventId;
        maybeFetchPending();
    }

    private void maybeFetchPending() {
        if (pendingEventId == null) {
            return;
        }
        if (userId == null || deviceId == null) {
            Log.d("EventDashboard", "Waiting for session: userId=" + userId + " deviceId=" + deviceId);
            return;
        }
        UUID eventId = pendingEventId;
        pendingEventId = null;
        Log.d("EventDashboard", "Fetching event: " + eventId);
        fetchAndDisplayEvent(eventId);
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
        userModel.getUserRaw(userId, deviceId)
                .addOnSuccessListener(this::loadFromUserData)
                .addOnFailureListener(ex -> {
                    Log.e("EventDashboard", "Failed to load user", ex);
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                        return;
                    }
                    Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_LONG).show();
                });
    }

    @SuppressWarnings("unchecked")
    private void loadFromUserData(java.util.Map<String, Object> userData) {
        if (userData == null) {
            return;
        }
        Object organizer = userData.get("organizer");
        if (!(organizer instanceof java.util.Map)) {
            Log.d("EventDashboard", "No organizer data for user");
            return;
        }
        Object created = ((java.util.Map<String, Object>) organizer).get("createdEvents");
        if (!(created instanceof java.util.List)) {
            Log.d("EventDashboard", "No createdEvents list");
            return;
        }
        for (Object id : (java.util.List<Object>) created) {
            UUID eventId = parseUUID(id == null ? null : id.toString());
            if (eventId != null) {
                fetchAndDisplayEvent(eventId);
            }
        }
    }

    private void fetchAndDisplayEvent(UUID eventId) {
        if (userId == null || deviceId == null) {
            Toast.makeText(requireContext(), "Missing user session", Toast.LENGTH_LONG).show();
            return;
        }
        eventModel.getEvent(eventId, userId, deviceId)
                .addOnSuccessListener(event -> {
                    if (event == null) {
                        return;
                    }
                    Log.d("EventDashboard", "Event data loaded: " + event.getEventName());
                    String title = stringValue(event.getEventName(), "Event");
                    String time = "🕐 " + formatLocalTime(event.getRegistrationStartTime());
                    String location = "🗺️ " + stringValue(event.getLocation(), "TBD");
                    EventCardItem item = new EventCardItem(eventId, title, time, location, null);
                    adapter.upsert(item);

                    UUID imageUuid = event.getImageId();
                    if (imageUuid != null) {
                        fetchAndAttachImage(eventId, item, imageUuid);
                    }
                })
                .addOnFailureListener(ex ->
                        {
                            Log.e("EventDashboard", "Failed to load event", ex);
                            Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_LONG).show();
                        }
                );
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

    private static UUID parseUUID(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
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
            Navigation.findNavController(requireView()).navigate(R.id.registerFragment);
        }
    }

    private final static UUID TAG = UUID.randomUUID();

    @NonNull
    @Override
    public UUID getUniqueTag() {
        return TAG;
    }
}
