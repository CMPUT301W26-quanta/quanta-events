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
import ca.quanta.quantaevents.databinding.FragmentEntrantEventHistoryBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.GeocoderUtil;
import ca.quanta.quantaevents.utils.ToastManager;
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
    private boolean hasLoaded = false;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to History and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("History");
        infoStore.setSubtitle("View enrolled event history");
        infoStore.setIconRes(R.drawable.material_symbols_history);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        // Initialize the adapter with a click listener to navigate to event details
        // for the event which is clicked
        adapter = new EventCardAdapter(item -> {
            NavDirections action = ca.quanta.quantaevents.fragments.EntrantEventHistoryFragmentDirections.actionEntrantEventHistoryFragmentToEventDetailsFragment(item.getEventId());
            Navigation.findNavController(requireView()).navigate(action);
        });
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventsRecyclerView.setAdapter(adapter);

        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        // check session
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            if (!hasLoaded) {
                hasLoaded = true;
                loadHistoryEvents();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEntrantEventHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // load events where the user was in the waitlist
    private void loadHistoryEvents() {
        if (userId == null || deviceId == null) {
            Log.d("EntrantEventHistory", "Session missing: userId/deviceId null");
            return;
        }
        Log.d("EntrantEventHistory", "Loading history events for user " + userId);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
            eventModel.getEvents(
                            userId,
                            deviceId,
                            50,
                            null,
                            EventViewModel.Fetch.HISTORY,
                            null,
                            null,
                            null,
                            null,
                            EventViewModel.SortBy.NAME)
                    .addOnSuccessListener(this::bindEventList)
                    .addOnFailureListener(ex -> {
                        if (!isAdded() || binding == null) return;
                        Log.e("EntrantEventHistory", "Failed to load events", ex);
                        if (ex instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException fex = (FirebaseFunctionsException) ex;
                            Log.e("EntrantEventHistory", "Functions code=" + fex.getCode() + " message=" + fex.getMessage());
                        }
                        if (isUserNotFound(ex)) {
                            handleMissingUser();
                            return;
                        }
                        ToastManager.show(getContext(), "Failed to load events", Toast.LENGTH_LONG);
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

    // bind the data to the view
    // The following function is from/based off OpenAI, ChatGPT, "bindEventList implementation for EntrantEventHistory", 2026-03-11
    private void bindEventList(List<Event> events) {
        if (!isAdded() || binding == null || adapter == null) return;
        if (events == null) {
            Log.d("EntrantEventHistory", "Event list is null");
            return;
        }
        Log.d("EntrantEventHistory", "Loaded events count=" + events.size());
        if (events.isEmpty()) {
            ToastManager.show(getContext(), "No event history", Toast.LENGTH_LONG);
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
                final Event finalEvent = event;
                GeocoderUtil.reverseGeocode(requireContext(), new LatLng(lat, lng), locationName ->
                        requireActivity().runOnUiThread(() -> {
                            if (!isAdded() || binding == null || adapter == null) return;
                            adapter.updateLocation(finalEvent.getEventId(), locationName);
                        })
                );
            }

            if (event.getImageId() != null) {
                fetchAndAttachImage(event.getEventId(), event.getImageId());
            }
        }
    }

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

    // The following function is from/based off OpenAI, ChatGPT, "decodeBase64ToBitmap which decodes base64 to a bitmap", 2026-03-11
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
