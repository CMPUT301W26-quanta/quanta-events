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

import com.google.android.gms.maps.model.LatLng;
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
import ca.quanta.quantaevents.databinding.FragmentAdminEventBrowserBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.GeocoderUtil;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

/**
 * Fragment for displaying UI for displaying events to admins.
 */
public class AdminEventBrowserFragment extends Fragment {
    private FragmentAdminEventBrowserBinding binding;
    private EventCardAdapter adapter;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;
    private boolean hasLoaded = false;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set the title as admin event browser
        // sets the subtitle and icon
        infoStore.setTitle("Admin Event Browser");
        infoStore.setSubtitle("Browse and Moderate Events");
        infoStore.setIconRes(R.drawable.material_symbols_calendar_lock_outline);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        // new instance of event card adapter and sends event id and true for fromAdmin
        // because admin is viewing from admin event browser
        adapter = new EventCardAdapter(item -> {
            Bundle args = new Bundle();
            args.putSerializable("eventId", item.getEventId());
            args.putBoolean("fromAdmin", true);
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_admineventbrowserfragment_to_eventdetailsfragment, args);
        });

        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventsRecyclerView.setAdapter(adapter);

        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        // checks and verifies session
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            if (!hasLoaded) {
                hasLoaded = true;
                loadAllEvents();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminEventBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Loads all past and current events for admins.
     */
    private void loadAllEvents() {
        if (userId == null || deviceId == null) {
            Log.d("AdminEventBrowser", "Session missing: userId/deviceId null");
            return;
        }

        Log.d("AdminEventBrowser", "Loading all events for admin " + userId);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(eventModel.getEvents(
                        userId,
                        deviceId,
                        -1,
                        null,
                        EventViewModel.Fetch.ALL,
                        null,
                        null,
                        null,
                        null,
                        EventViewModel.SortBy.REGISTRATION_END)
                .addOnSuccessListener(events -> {
                    if (!isAdded() || binding == null) return;
                    bindEventList(events);
                })
                .addOnFailureListener(ex -> {
                    if (!isAdded() || binding == null) return;
                    Log.e("AdminEventBrowser", "Failed to load events", ex);
                    if (ex instanceof FirebaseFunctionsException) {
                        FirebaseFunctionsException fex = (FirebaseFunctionsException) ex;
                        Log.e("AdminEventBrowser", "Functions code=" + fex.getCode() + " message=" + fex.getMessage());
                    }
                    if (isUserNotFound(ex)) {
                        handleMissingUser();
                        return;
                    }
                    if (isAdded()) {
                        ToastManager.show(getContext(), "Failed to load events", Toast.LENGTH_LONG);
                    }
                })
        );

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        hasLoaded = false;
        binding = null;
        adapter = null;
    }

    /**
     * The following function is from OpenAI, ChatGPT, "bindEventList implementation under AdminEventBrowser", 2026-03-12
     * Binds the event list to the view.
     * @param events
     */
    private void bindEventList(List<Event> events) {
        if (!isAdded() || binding == null || adapter == null) return;
        if (events == null) {
            Log.d("AdminEventBrowser", "Event list is null");
            return;
        }

        Log.d("AdminEventBrowser", "Loaded events count=" + events.size());

        if (events.isEmpty()) {
            ToastManager.show(getContext(), "No events to moderate", Toast.LENGTH_LONG);
            adapter.setItems(new ArrayList<>());
            return;
        }

        ArrayList<EventCardItem> items = new ArrayList<>();

        for (Event event : events) {
            if (event == null) continue;
            UUID eventId = event.getEventId();
            String title = stringValue(event.getEventName(), "Event");
            String time = formatLocalTime(event.getEventTime());
            Double lat = event.getLocationLat();
            Double lng = event.getLocationLng();
            String location = (lat != null && lng != null)
                    ? String.format("%.5f, %.5f", lat, lng)
                    : "TBD";
            items.add(new EventCardItem(eventId, title, time, location, null));
        }

        adapter.setItems(items);

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event == null) continue;

            Double lat = event.getLocationLat();
            Double lng = event.getLocationLng();
            if (lat != null && lng != null) {
                final UUID eventId = event.getEventId();
                GeocoderUtil.reverseGeocode(requireContext(), new LatLng(lat, lng), locationName ->
                        requireActivity().runOnUiThread(() -> {
                            if (!isAdded() || binding == null || adapter == null) return;
                            adapter.updateLocation(eventId, locationName);
                        })
                );
            }

            if (event.getImageId() != null) {
                fetchAndAttachImage(event.getEventId(), event.getImageId());
            }
        }
    }

    /**
     * Fetches an image and attaches it to an event.
     * @param eventId UUID identifying event.
     * @param imageId UUID identifying image.
     */
    private void fetchAndAttachImage(UUID eventId, UUID imageId) {
        imageModel.getImage(imageId, userId, deviceId)
                .addOnSuccessListener(data -> {
                    if (!isAdded() || binding == null || adapter == null) return;
                    Object imageData = data.getImageData();
                    if (imageData == null) return;
                    Bitmap bitmap = decodeBase64ToBitmap(imageData.toString());
                    if (bitmap != null) {
                        adapter.updateImage(eventId, bitmap);
                    }
                });
    }

    /**
     * Converts a value to a string.
     * @param value Value to be converted.
     * @param fallback Default string in case conversion fails.
     * @return Converted string.
     */
    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String result = value.toString().trim();
        return result.isEmpty() ? fallback : result;
    }

    /**
     * Formats zoned date time as a string.
     * @param value Date time to be formatted.
     * @return String representation of date time.
     */
    private String formatLocalTime(@Nullable ZonedDateTime value) {
        if (value == null) {
            return "TBD";
        }
        ZonedDateTime local = value.withZoneSameInstant(ZoneId.systemDefault());
        return local.format(displayFormatter);
    }

    /**
     * Decodes a base64 representation of an image to a bitmap.
     * @param base64 String representing image data.
     * @return Bitmap of image data if successful, null otherwise.
     */
    private static Bitmap decodeBase64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Checks if a user is not found.
     * @param ex Exception to be checked.
     * @return true if the exception is a not found error, false otherwise.
     */
    private boolean isUserNotFound(Exception ex) {
        if (ex instanceof FirebaseFunctionsException) {
            FirebaseFunctionsException.Code code = ((FirebaseFunctionsException) ex).getCode();
            return code == FirebaseFunctionsException.Code.NOT_FOUND;
        }
        return false;
    }

    /**
     * Clears the session and redirects to registration fragment if the user does not exist.
     */
    private void handleMissingUser() {
        sessionStore.clearSession();
        if (isAdded()) {
            NavDirections action = AdminEventBrowserFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

}
