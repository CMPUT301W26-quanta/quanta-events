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
import ca.quanta.quantaevents.databinding.FragmentEntrantEventBrowserBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class EntrantEventBrowserFragment extends Fragment {
    private FragmentEntrantEventBrowserBinding binding;
    private EventCardAdapter adapter;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private SessionStore sessionStore;
    private UUID userId;
    private UUID deviceId;
    private boolean loadedInitialEvents = false;
    private boolean hasLoaded = false;
    private String filterFrom = null;
    private String filterTo = null;
    private String filterCategory = null;
    private Integer filterCapacity = null;

    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("EntrantEventBrowser", "onViewCreated");

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Event Browser and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Event Browser");
        infoStore.setSubtitle("Browse Events to enroll");
        infoStore.setIconRes(R.drawable.material_symbols_search_outline);

        // back button click listener
        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
        // filter button click listener to navigate to filter fragment
        binding.filterButton.setOnClickListener(
                v -> {
                    NavDirections action = EntrantEventBrowserFragmentDirections.actionEntranteventbrowserfragmentToEventfilterfragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );
        binding.qrButton.setOnClickListener(
                v -> {
                    NavDirections action = EntrantEventBrowserFragmentDirections.actionEntranteventbrowserfragmentToEventqrcodefragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );

        adapter = new EventCardAdapter(item -> {
            NavDirections action = EntrantEventBrowserFragmentDirections.actionEventBrowserFragmentToEventDetailsFragment(item.getEventId());
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
            if (!hasLoaded) {
                hasLoaded = true;

                Object savedFilters = Navigation.findNavController(requireView())
                        .getCurrentBackStackEntry()
                        .getSavedStateHandle()
                        .get("filters");
                if (savedFilters != null) {
                    Bundle filters = (Bundle) savedFilters;
                    filterFrom = filters.getString("from");
                    filterTo = filters.getString("to");
                    filterCategory = filters.getString("category");
                    filterCapacity = filters.getInt("capacity");

                    Navigation.findNavController(requireView())
                            .getCurrentBackStackEntry()
                            .getSavedStateHandle()
                            .remove("filters");
                }

                maybeLoadAllEvents();
            }
        });
        Navigation.findNavController(requireView())
                .getCurrentBackStackEntry()
                .getSavedStateHandle()
                .getLiveData("filters")
                .observe(getViewLifecycleOwner(), result -> {
                    if (result == null) return;
                    Bundle filters = (Bundle) result;
                    filterFrom = filters.getString("from");
                    filterTo = filters.getString("to");
                    filterCategory = filters.getString("category");
                    filterCapacity = filters.getInt("capacity");

                    Navigation.findNavController(requireView())
                            .getCurrentBackStackEntry()
                            .getSavedStateHandle()
                            .remove("filters");

                    loadAvailableEvents();
                });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("EntrantEventBrowser", "onCreateView");
        binding = FragmentEntrantEventBrowserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        hasLoaded = false;
        loadedInitialEvents = false;
        binding = null;
        adapter = null;
    }

    // decides if events should be loaded based on if they
    // have been loaded before or if session is valid
    private void maybeLoadAllEvents() {
        if (loadedInitialEvents) {
            return;
        }
        if (userId == null || deviceId == null) {
            return;
        }
        loadedInitialEvents = true;
        loadAvailableEvents();
    }

    // loads all the available events based on the user session and
    // by default sorts them by registration end time.
    private void loadAvailableEvents() {
        if (userId == null || deviceId == null) {
            Log.d("EntrantEventBrowser", "Session missing: userId/deviceId null");
            return;
        }
        Log.d("EntrantEventBrowser", "Loading available events for user " + userId + deviceId);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
            eventModel.getEvents(
                            userId,
                            deviceId,
                            50,
                            null,
                            EventViewModel.Fetch.AVAILABLE,
                            filterFrom,
                            filterTo,
                            filterCategory,
                            filterCapacity,
                            EventViewModel.SortBy.REGISTRATION_START)
                    .addOnSuccessListener(events -> {
                        if (!isAdded() || binding == null) return;
                        bindEventList(events);
                    })
                    .addOnFailureListener(ex -> {
                        if (!isAdded() || binding == null) return;
                        Log.e("EntrantEventBrowser", "Failed to load events", ex);
                        if (ex instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException fex = (FirebaseFunctionsException) ex;
                            Log.e("EntrantEventBrowser", "Functions code=" + fex.getCode() + " message="
                                    + fex.getMessage());
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
    //bind the events to the array adapter and the card item
    // The following function is from OpenAI, ChatGPT, "bindEventList implementation for EntrantEventBrowser", 2026-03-11

    private void bindEventList(List<Event> events) {
        if (!isAdded() || binding == null || adapter == null) return;
        if (events == null) {
            Log.d("EntrantEventBrowser", "Event list is null");
            return;
        }
        ToastManager.cancel();
        Log.d("EntrantEventBrowser", "Loaded events count=" + events.size());
        if (events.isEmpty()) {
            ToastManager.show(getContext(), "No events to enroll in", Toast.LENGTH_LONG);
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
                    if (!isAdded() || binding == null || adapter == null) return;
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

    // The following function is from OpenAI, ChatGPT, "decodeBase64ToBitmap which decodes base64 to a bitmap", 2026-03-11
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
            NavDirections action = EntrantEventBrowserFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
        }
    }
}
