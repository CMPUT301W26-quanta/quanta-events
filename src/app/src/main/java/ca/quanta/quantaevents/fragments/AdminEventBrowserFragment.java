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
import ca.quanta.quantaevents.databinding.FragmentAdminEventBrowserBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

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

    // loads all events past and ongoing for admin to view and moderate them.

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
                        EventViewModel.SortBy.REGISTRATION_END)
                .addOnSuccessListener(events -> {
                    if (!isAdded()) {
                        return;
                    }
                    bindEventList(events);
                })
                .addOnFailureListener(ex -> {
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
    }

    // binds the event list to the view
    // The following function is from OpenAI, ChatGPT, "bindEventList implementation under AdminEventBrowser", 2026-03-12
    private void bindEventList(List<Event> events) {
        if (!isAdded()) {
            return;
        }
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
            if (event == null) {
                continue;
            }

            UUID eventId = event.getEventId();
            String title = stringValue(event.getEventName(), "Event");
            String time = formatLocalTime(event.getEventTime());
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
                    if (!isAdded()) {
                        return;
                    }
                    Object imageData = data.getImageData();
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
            NavDirections action = AdminEventBrowserFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
    }

}
