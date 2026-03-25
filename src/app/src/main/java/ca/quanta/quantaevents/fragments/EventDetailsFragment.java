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
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

    private UserViewModel model;
    private boolean isOrganizer = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up the header

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);

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
                                if (isAdded()) {
                                    ToastManager.show(requireContext(), "Failed to load event", Toast.LENGTH_LONG);
                                    Navigation.findNavController(requireView()).popBackStack();
                                }
                            }
                    )
        );
    }

    // bind the event details to the
    // ui elemtents in the view
    private void bindEvent(Event event) {
        if (event == null) {
            return;
        }

        geolocationRequired = event.isGeolocationEnabled();

        binding.textEventTitle.setText(stringValue(event.getEventName(), "Event"));

        String organizer = event.getOrganizerId() == null ? "Unknown" : event.getOrganizerId().toString();
        System.out.println(organizer);
        binding.eventStartTime.setText(" " + formatLocalTime(event.getRegistrationStartTime()));
        binding.textLocation.setText(" " + stringValue(event.getLocation(), "TBD"));

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
            return;
        }
        if (userId != null && organizerId != null && organizerId.equals(userId.toString())) {
            isOrganizer = true;
            binding.enrollButton.setText("Manage");
            binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
            binding.enrollButton.setOnClickListener(v -> {
                NavDirections action = EventDetailsFragmentDirections.actionEventdetailsfragmentToEventmanagerfragment(eventId);
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
            binding.textOrganizer.setText(" [unable to fetch organizer name]");
            return;
        }

        binding.textOrganizer.setText(" Loading organizer name...");

        eventModel.getOrganizerName(userId, deviceId, eventId)
                .addOnSuccessListener(name -> {
                    if (!isAdded()) {
                        binding.textOrganizer.setText(" [no associated organizer]");
                        return;
                    }

                    if (name == null) {
                        name = "[organizer's username is null]";
                    } else if (name.trim().isEmpty()) {
                        name = "[organizer has empty name]";
                    }

                    binding.textOrganizer.setText(" Organized by " + name.trim());
                })
                .addOnFailureListener(exception -> {
                    ToastManager.show(requireContext(), "Failed to fetch organizer name", Toast.LENGTH_LONG);
                    Log.e("EventDetailsFragment", "Failed to fetch organizer name.", exception);

                    // set the organizer id as the name instead,
                    // to have something there at least, and to possibly help w/ debugging
                    binding.textOrganizer.setText(" Organized by " + event.getOrganizerId());
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
                    waitlistCount = count == null ? 0 : count;
                    updateWaitlistText();
                })
                .addOnFailureListener(ex ->
                        Log.e("EventDetails", "refreshWaitlistCount: failed", ex)
                );
    }

    private void updateWaitlistText() {
        if (!isAdded()) {
            return;
        }
        binding.textWaitingList.setText(" Wait list: " + waitlistCount);
    }

    private void updateEnrollButtonLabel() {
        if (!isAdded()) {
            return;
        }
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

    private void performNormalJoin() {
        Log.d("EventDetails", "joinWaitlist: eventId=" + eventId); // keep the current join logging

        eventModel.joinWaitlist(userId, deviceId, eventId) // keep the existing normal join request
                .addOnSuccessListener(_done -> {
                    inWaitlist = true; // update local state after a successful join
                    Log.d("EventDetails", "joinWaitlist: success"); // keep success logging
                    updateEnrollButtonLabel(); // refresh the button text/color
                    refreshWaitlistCount(); // refresh the displayed waitlist count
                    ToastManager.show(requireContext(), "Joined waitlist", Toast.LENGTH_LONG); // show the same success toast
                })
                .addOnFailureListener(ex -> {
                    Log.e("EventDetails", "joinWaitlist: failed", ex); // keep failure logging
                    ToastManager.show(requireContext(), "Failed to join waitlist", Toast.LENGTH_LONG); // show the same failure toast
                });
    }

    private void startGeolocationJoinFlow() {
        ToastManager.show(
                requireContext(),
                "This event requires geolocation. Geolocation flow will be added here.", // placeholder for the next step
                Toast.LENGTH_LONG
        );
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
                        inWaitlist = false;
                        Log.d("EventDetails", "leaveWaitlist: success");
                        updateEnrollButtonLabel();
                        refreshWaitlistCount();
                        ToastManager.show(requireContext(), "Left waitlist", Toast.LENGTH_LONG);
                    })
                    .addOnFailureListener(ex ->
                            {
                                Log.e("EventDetails", "leaveWaitlist: failed", ex);
                                ToastManager.show(requireContext(), "Failed to leave waitlist", Toast.LENGTH_LONG);
                            }
                    );
        } else {
            if (!geolocationRequired) { // if this event does not require geolocation, keep the old join behavior
                performNormalJoin(); // use the extracted normal join method
            } else {
                startGeolocationJoinFlow(); // send geolocation-enabled events into a separate placeholder flow
            }
        }
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
