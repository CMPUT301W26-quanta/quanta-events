package ca.quanta.quantaevents.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.burger.SmartBurger;
import ca.quanta.quantaevents.databinding.FragmentEventDetailsBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.functions.FirebaseFunctionsException;

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
    private boolean inWaitlist = false;
    private boolean geolocationRequired = false;
    private int waitlistCount = 0;

    private boolean isDrawn = false;
    private boolean isPrivate = false;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    private UserViewModel model;
    private boolean isOrganizer = false;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);

        binding.getRoot().setVisibility(INVISIBLE);

        // set title of the page to Event and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Event");
        infoStore.setSubtitle("View event details");
        infoStore.setIconRes(R.drawable.material_symbols_event_outline);

        // set up the view model

        this.model = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        //set up the session store and get this user

        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);

        // reads event id passed in form of arguments using bundles
        readEventId();

        // check session
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            loadEvent();
        });

        // gets role to determine which button to display
        sessionStore.getRoleMask().observe(getViewLifecycleOwner(), mask -> {
            isAdmin = mask != null && (mask & SmartBurger.ADMIN_GROUP) != 0;
            updateManageButton(null);
        });

        // set up back button on click listener

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> {
                            boolean fineGranted = Boolean.TRUE.equals(
                                    result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                            boolean coarseGranted = Boolean.TRUE.equals(
                                    result.get(Manifest.permission.ACCESS_COARSE_LOCATION));

                            if (fineGranted || coarseGranted) {
                                fetchLocationAndJoinWaitlist();
                            } else {
                                ToastManager.show(
                                        getContext(),
                                        "Location permission is required for this event",
                                        Toast.LENGTH_LONG
                                );
                            }
                        }
                );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    // read event id passed in form of arguments
    // also read if the user was redirect to his fragment from the admin event browser
    private void readEventId() {
        EventDetailsFragmentArgs args = EventDetailsFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
        fromAdmin = args.getFromAdmin();
    }

    // load the event details
    private void loadEvent() {
        if (userId == null || deviceId == null) {
            NavDirections action = EventDetailsFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
            return;
        }
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
            eventModel.getEvent(eventId, userId, deviceId)
                    .addOnSuccessListener(this::bindEvent)
                    .addOnFailureListener(ex -> {
                        if (!isAdded() || binding == null) return;
                        ToastManager.show(getContext(), "Failed to load event", Toast.LENGTH_LONG);
                        Navigation.findNavController(requireView()).popBackStack();
                    })
        );
    }

    // bind the event details to the
    // ui elemtents in the view
    private void bindEvent(Event event) {
        if (!isAdded() || binding == null) return;
        if (event == null) {
            return;
        }

        geolocationRequired = event.isGeolocationEnabled();
        isDrawn = event.isDrawn();
        isPrivate = event.isPrivate();

        binding.getRoot().setVisibility(VISIBLE);

        binding.textEventTitle.setText(stringValue(event.getEventName(), "Event"));

        String organizer = event.getOrganizerId() == null ? "Unknown" : event.getOrganizerId().toString();
        System.out.println(organizer);
        binding.eventStartTime.setText(" " + formatLocalTime(event.getRegistrationStartTime()));
        binding.textLocation.setText(" " + stringValue(event.getLocation(), "TBD"));
        binding.registrationEndTime.setText(" " + formatLocalTime(event.getRegistrationEndTime()));
        binding.registrationStartTime.setText(" " + formatLocalTime(event.getRegistrationStartTime()));
        binding.textGuidelines.setText(" " + stringValue(event.getEventGuidelines(), "No Event Guidelines!"));
        waitlistCount = event.getWaitList() == null ? 0 : event.getWaitList().size();
        updateWaitlistText();

        binding.textDescription.setText(stringValue(event.getEventDescription(), ""));

        updateManageButton(organizer);
        fetchOrganizerName(event);
        updateWaitlistState();
        refreshWaitlistCount();

        UUID imageUuid = event.getImageId();
        if (imageUuid != null) {
            imageModel.getImage(imageUuid, userId, deviceId)
                    .addOnSuccessListener(imageData -> {
                        if (!isAdded() || binding == null) return;
                        Object imageBase64 = imageData.getImageData();
                        if (imageBase64 != null) {
                            Bitmap bitmap = decodeBase64ToBitmap(imageBase64.toString());
                            if (bitmap != null) {
                                binding.imagePreview.setImageBitmap(bitmap);
                            }
                        }
                    });
        }
    }

    // change text on the button to display according to role
    private void updateManageButton(String organizerId) {
        if (isAdmin && fromAdmin) {
            binding.enrollButton.setText("Delete");
            binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
            isOrganizer = false;

            binding.enrollButton.setOnClickListener(v -> {
                this.eventModel.deleteEvent(this.eventId, this.userId, this.deviceId)
                        .addOnSuccessListener(success -> {
                            if (!isAdded() || binding == null) return;
                            Navigation.findNavController(v).popBackStack();
                        })
                        .addOnFailureListener(exception -> {
                            if (!isAdded() || binding == null) return;
                            Log.e("EventDetailsFragment", "Failed to delete event.", exception);

                            ToastManager.show(requireContext(), "Failed to delete event.", Toast.LENGTH_LONG);

                            if (exception instanceof FirebaseFunctionsException) {
                                Log.e("EventDetailsFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                            }
                        });
            });

            return;
        }

        if (userId != null && organizerId != null && organizerId.equals(userId.toString())) {
            isOrganizer = true;
            binding.enrollButton.setText("Manage");
            binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
            binding.enrollButton.setOnClickListener(v -> {
                NavDirections action = EventDetailsFragmentDirections.actionEventdetailsfragmentToEventmanagerfragment(eventId, isDrawn, isPrivate);
                Navigation.findNavController(v).navigate(action);
            });

            return;
        }

        isOrganizer = false;
        binding.enrollButton.setOnClickListener(v -> toggleWaitlist());
    }

    // get the organizer name to display in the ui
    private void fetchOrganizerName(Event event) {
        if (userId == null || deviceId == null || eventId == null) {
            if (isAdded()) binding.textOrganizer.setText(" [unable to fetch organizer name]");
            return;
        }

        if (isAdded()) binding.textOrganizer.setText(" Loading organizer name...");

        eventModel.getOrganizerName(userId, deviceId, eventId)
                .addOnSuccessListener(name -> {
                    if (!isAdded() || binding == null) return;

                    if (name == null) {
                        name = "[organizer's username is null]";
                    } else if (name.trim().isEmpty()) {
                        name = "[organizer has empty name]";
                    }

                    if (isAdded()) binding.textOrganizer.setText(" Organized by " + name.trim());
                })
                .addOnFailureListener(exception -> {
                    if (!isAdded() || binding == null) return;
                    ToastManager.show(getContext(), "Failed to fetch organizer name", Toast.LENGTH_LONG);
                    Log.e("EventDetailsFragment", "Failed to fetch organizer name.", exception);

                    // set the organizer id as the name instead,
                    // to have something there at least, and to possibly help w/ debugging
                    if (isAdded()) binding.textOrganizer.setText(" Organized by " + event.getOrganizerId());
                });
    }

    private void updateWaitlistState() {
        if (isOrganizer || (isAdmin && fromAdmin)) {
            return;
        }
        if (userId == null || deviceId == null || eventId == null) {
            Log.d("EventDetails", "updateWaitlistState: missing session or eventId");
            return;
        }
        Log.d("EventDetails", "updateWaitlistState: checking waitlist for eventId=" + eventId);
        eventModel.checkWaitlist(userId, deviceId, eventId)
                .addOnSuccessListener(result -> {
                    if (!isAdded() || binding == null) return;
                    inWaitlist = Boolean.TRUE.equals(result);
                    Log.d("EventDetails", "inWaitlist result=" + inWaitlist);
                    updateEnrollButtonLabel();
                });
    }

    private void refreshWaitlistCount() {
        if (userId == null || deviceId == null || eventId == null) {
            Log.d("EventDetails", "refreshWaitlistCount: missing session or eventId");
            return;
        }
        eventModel.getWaitlistCount(userId, deviceId, eventId)
                .addOnSuccessListener(count -> {
                    if (!isAdded() || binding == null) return;
                    waitlistCount = count == null ? 0 : count;
                    updateWaitlistText();
                })
                .addOnFailureListener(ex ->
                        Log.e("EventDetails", "refreshWaitlistCount: failed", ex)
                );
    }

    private void updateWaitlistText() {
        if (!isAdded() || binding == null) return;
        binding.textWaitingList.setText(" Wait list: " + waitlistCount);
    }

    private void updateEnrollButtonLabel() {
        if (!isAdded() || binding == null) return;
        if (isOrganizer || (isAdmin && fromAdmin)) {
            return;
        }
        if (inWaitlist) {
            binding.enrollButton.setText("Leave Waitlist");
            binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
        } else {
            binding.enrollButton.setText("Join Waitlist");
            binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_green));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        binding = null;
    }

    private void toggleWaitlist() {
        if (userId == null || deviceId == null || eventId == null) {
            Log.d("EventDetails", "toggleWaitlist: missing session or eventId");
            return;
        }
        if (inWaitlist) {
            Log.d("EventDetails", "leaveWaitlist: eventId=" + eventId);
            eventModel.leaveWaitlist(userId, deviceId, eventId)
                    .addOnSuccessListener(_done -> {
                        if (!isAdded() || binding == null) return;
                        inWaitlist = false;
                        Log.d("EventDetails", "leaveWaitlist: success");
                        updateEnrollButtonLabel();
                        refreshWaitlistCount();
                        ToastManager.show(getContext(), "Left waitlist", Toast.LENGTH_LONG);
                    })
                    .addOnFailureListener(ex ->
                            {
                                if (!isAdded() || binding == null) return;
                                Log.e("EventDetails", "leaveWaitlist: failed", ex);
                                ToastManager.show(getContext(), "Failed to leave waitlist", Toast.LENGTH_LONG);
                            }
                    );
        } else {
            Log.d("EventDetails", "joinWaitlist: eventId=" + eventId);

            if (geolocationRequired) {
                requestLocationThenJoin();
            } else {
                submitJoinWaitlist(null, null, null);
            }
        }
    }

    private void requestLocationThenJoin() {
        boolean fineGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {
            fetchLocationAndJoinWaitlist();
            return;
        }

        locationPermissionLauncher.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void fetchLocationAndJoinWaitlist() {
        boolean fineGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {
            ToastManager.show(
                    getContext(),
                    "Location permission is required for this event",
                    Toast.LENGTH_LONG
            );
            return;
        }

        fusedLocationClient

                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (!isAdded() || binding == null) return;
                    if (location == null) {
                        ToastManager.show(
                                getContext(),
                                "Failed to get current location",
                                Toast.LENGTH_LONG
                        );
                        return;
                    }

                    submitJoinWaitlist(
                            location.getLatitude(),
                            location.getLongitude(),
                            (double) location.getAccuracy()
                    );
                })
                .addOnFailureListener(ex -> {
                    if (!isAdded() || binding == null) return;
                    Log.e("EventDetails", "getCurrentLocation failed", ex);
                    ToastManager.show(
                            getContext(),
                            "Failed to get current location",
                            Toast.LENGTH_LONG
                    );
                });
    }

    private void submitJoinWaitlist(
            @Nullable Double latitude,
            @Nullable Double longitude,
            @Nullable Double accuracyM
    ) {
        eventModel.joinWaitlist(userId, deviceId, eventId, latitude, longitude, accuracyM)
                .addOnSuccessListener(_done -> {
                    if (!isAdded() || binding == null) return;
                    inWaitlist = true;
                    Log.d("EventDetails", "joinWaitlist: success");
                    updateEnrollButtonLabel();
                    refreshWaitlistCount();
                    ToastManager.show(getContext(), "Joined waitlist", Toast.LENGTH_LONG);
                })
                .addOnFailureListener(ex -> {
                    if (!isAdded() || binding == null) return;
                    Log.e("EventDetails", "joinWaitlist: failed", ex);
                    ToastManager.show(getContext(), ex.getMessage(), Toast.LENGTH_LONG);
                });
    }

    /** formats the time returned from server to show in a user readble format
     * i.e. converts the time zone
    */
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
    // The following function is from/based off OpenAI, ChatGPT, "decodeBase64ToBitmap which decodes base64 to a bitmap", 2026-03-11
    private static Bitmap decodeBase64ToBitmap(String base64) {
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
