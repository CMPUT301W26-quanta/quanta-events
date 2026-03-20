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
import ca.quanta.quantaevents.burger.SmartBurgerState;
import ca.quanta.quantaevents.burger.Tagged;
import ca.quanta.quantaevents.databinding.FragmentEntrantEventListBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class EntrantEventListFragment extends Fragment implements Tagged {
    private FragmentEntrantEventListBinding binding;
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
        Log.d("EntrantEventList", "onViewCreated");

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Event list and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Event List");
        infoStore.setSubtitle("View events in which you are enrolled");
        infoStore.setIconRes(R.drawable.material_symbols_event_list_outline);

        binding.historyButton.setOnClickListener(
                v -> {
                    NavDirections action = ca.quanta.quantaevents.fragments.EntrantEventListFragmentDirections.actionEntranteventlistToEntranteventhistoryfragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );
        binding.searchButton.setOnClickListener(
                v -> {
                    NavDirections action = ca.quanta.quantaevents.fragments.EntrantEventListFragmentDirections.actionEntranteventlistToEntranteventbrowserfragment();
                    Navigation.findNavController(v).navigate(action);
                }
        );

        adapter = new EventCardAdapter(item -> {
            NavDirections action = ca.quanta.quantaevents.fragments.EntrantEventListFragmentDirections.actionEntranteventlistToEventdetailsfragment(item.getEventId());
            Navigation.findNavController(requireView()).navigate(action);
        });
        binding.eventsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.eventsRecyclerView.setAdapter(adapter);
        Log.d("EntrantEventList", "RecyclerView adapter attached");

        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            Log.d("EntrantEventList", "Session updated userId=" + userId + " deviceId=" + deviceId);
            loadEnrolledEvents();
        });

        new ViewModelProvider(requireActivity()).get(SmartBurgerState.class).show(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEntrantEventListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void loadEnrolledEvents() {
        if (userId == null || deviceId == null) {
            Log.d("EntrantEventList", "Session missing: userId/deviceId null");
            return;
        }
        Log.d("EntrantEventList", "Loading enrolled events for user " + userId);
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
            eventModel.getEvents(
                            userId,
                            deviceId,
                            50,
                            null,
                            EventViewModel.Fetch.IN,
                            null,
                            null,
                            null,
                            null)
                    .addOnSuccessListener(this::bindEventList)
                    .addOnFailureListener(ex -> {
                        Log.e("EntrantEventList", "Failed to load events", ex);
                        if (ex instanceof FirebaseFunctionsException) {
                            FirebaseFunctionsException fex = (FirebaseFunctionsException) ex;
                            Log.e("EntrantEventList", "Functions code=" + fex.getCode() + " message=" + fex.getMessage());
                        }
                        if (isUserNotFound(ex)) {
                            handleMissingUser();
                            return;
                        }
                        Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_LONG).show();
                    })
        );
    }

    private void bindEventList(List<Event> events) {
        if (events == null) {
            Log.d("EntrantEventList", "Event list is null");
            return;
        }
        Log.d("EntrantEventList", "Loaded events count=" + events.size());
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
            NavDirections action = EntrantEventListFragmentDirections.actionGlobalRegisterFragment();
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
