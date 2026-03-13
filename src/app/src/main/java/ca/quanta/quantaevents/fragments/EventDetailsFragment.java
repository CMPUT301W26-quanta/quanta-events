package ca.quanta.quantaevents.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.burger.SmartBurger;
import ca.quanta.quantaevents.databinding.FragmentEventDetailsBinding;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class EventDetailsFragment extends Fragment {
    private FragmentEventDetailsBinding binding;
    private SessionStore sessionStore;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private UUID userId;
    private UUID deviceId;
    private UUID eventId;
    private boolean isAdmin = false;
    private boolean fromAdmin = false;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event");
        infoStore.setSubtitle("View event details");
        infoStore.setIconRes(R.drawable.material_symbols_event_outline);

        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        readEventId();
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            loadEvent();
        });
        sessionStore.getRoleMask().observe(getViewLifecycleOwner(), mask -> {
            isAdmin = mask != null && (mask & SmartBurger.ADMIN_GROUP) != 0;
            updateManageButton(null);
        });

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void readEventId() {
        EventDetailsFragmentArgs args = EventDetailsFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
        fromAdmin = args.getFromAdmin();
    }

    private void loadEvent() {
        if (userId == null || deviceId == null) {
            NavDirections action = EventDetailsFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
            return;
        }
        eventModel.getEvent(eventId, userId, deviceId)
                .addOnSuccessListener(this::bindEvent)
                .addOnFailureListener(ex -> {
                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_LONG).show();
                                Navigation.findNavController(requireView()).popBackStack();
                            }
                        }
                );
    }

    private void bindEvent(Event event) {
        if (event == null) {
            return;
        }
        binding.textEventTitle.setText(stringValue(event.getEventName(), "Event"));
        String organizer = event.getOrganizerId() == null ? "Unknown" : event.getOrganizerId().toString();
        binding.textOrganizer.setText(" Organized by " + organizer);
        binding.textDateTime.setText(" " + formatLocalTime(event.getRegistrationStartTime()));
        binding.textLocation.setText(" " + stringValue(event.getLocation(), "TBD"));

        int waitCount = event.getWaitList() == null ? 0 : event.getWaitList().size();
        binding.textWaitingList.setText(" Wait list: " + waitCount);

        binding.textDescription.setText(stringValue(event.getEventDescription(), ""));

        updateManageButton(organizer);

        UUID imageUuid = event.getImageId();
        if (imageUuid != null) {
            imageModel.getImage(imageUuid, userId, deviceId)
                    .addOnSuccessListener(imageData -> {
                        Object imageBase64 = imageData.get("imageData");
                        if (imageBase64 != null) {
                            Bitmap bitmap = decodeBase64ToBitmap(imageBase64.toString());
                            if (bitmap != null) {
                                binding.imagePreview.setImageBitmap(bitmap);
                            }
                        }
                    });
        }
    }

    private void updateManageButton(String organizerId) {
        if (isAdmin && fromAdmin) {
            binding.enrollButton.setText("Delete");
            binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
            return;
        }
        if (userId != null && organizerId != null && organizerId.equals(userId.toString())) {
            binding.enrollButton.setText("Manage");
            binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
            binding.enrollButton.setOnClickListener(v -> {
                NavDirections action = EventDetailsFragmentDirections.actionEventdetailsfragmentToEventmanagerfragment(eventId);
                Navigation.findNavController(v).navigate(action);
            });
        }
    }

    private String formatLocalTime(@Nullable ZonedDateTime value) {
        if (value == null) {
            return "TBD";
        }
        ZonedDateTime local = value.withZoneSameInstant(ZoneId.systemDefault());
        return local.format(displayFormatter);
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String result = value.toString().trim();
        return result.isEmpty() ? fallback : result;
    }

    private static Bitmap decodeBase64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
