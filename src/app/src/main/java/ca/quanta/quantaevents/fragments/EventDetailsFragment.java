package ca.quanta.quantaevents.fragments;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.adapters.CommentAdapter;
import ca.quanta.quantaevents.adapters.ImageCardAdapter;
import ca.quanta.quantaevents.adapters.NotificationAdapter;
import ca.quanta.quantaevents.burger.SmartBurger;
import ca.quanta.quantaevents.databinding.FragmentEventDetailsBinding;
import ca.quanta.quantaevents.loading.LoaderState;
import ca.quanta.quantaevents.models.Comment;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.models.Notification;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.CommentViewModel;
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

    private CommentViewModel commentModel;

    private CommentAdapter commentAdapter;


    private UUID userId;
    private UUID deviceId;
    private UUID eventId;
    private boolean isAdmin = false;
    private boolean fromAdmin = false;
    private boolean inWaitlist = false;
    private boolean geolocationRequired = false;
    private int waitlistCount = 0;

    private boolean isDrawn = false;
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

        this.binding.getRoot().setVisibility(INVISIBLE);

        // set title of the page to Event and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Event");
        infoStore.setSubtitle("View event details");
        infoStore.setIconRes(R.drawable.material_symbols_event_outline);

        // set up the view model

        this.model = new ViewModelProvider(requireActivity()).get(UserViewModel.class);


        //set up the session store and get this user

        this.sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        this.eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        this.imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        this.commentModel = new ViewModelProvider(this).get(CommentViewModel.class);


        // reads event id passed in form of arguments using bundles
        readEventId();

        // check session
        this.sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            this.userId = uid;
            this.deviceId = did;
            loadEvent();
        });

        // gets role to determine which button to display
        this.sessionStore.getRoleMask().observe(getViewLifecycleOwner(), mask -> {
            isAdmin = mask != null && (mask & SmartBurger.ADMIN_GROUP) != 0;
            updateManageButton(null);
        });


        // set up back button on click listener

        this.binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.locationPermissionLauncher =
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
                                        requireContext(),
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
        this.binding = FragmentEventDetailsBinding.inflate(inflater, container, false);
        return this.binding.getRoot();
    }

    // read event id passed in form of arguments
    // also read if the user was redirect to his fragment from the admin event browser
    private void readEventId() {
        EventDetailsFragmentArgs args = EventDetailsFragmentArgs.fromBundle(getArguments());
        this.eventId = args.getEventId();
        this.fromAdmin = args.getFromAdmin();
    }

    // load the event details
    private void loadEvent() {
        if (this.userId == null || this.deviceId == null) {
            NavDirections action = EventDetailsFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
            return;
        }
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
            this.eventModel.getEvent(this.eventId, this.userId, this.deviceId)
                    .addOnSuccessListener(event -> {this.bindEvent(event);loadComments(); })
                    .addOnFailureListener(ex -> {
                                if (isAdded()) {
                                    ToastManager.show(requireContext(), "Failed to load event", Toast.LENGTH_LONG);
                                    Navigation.findNavController(requireView()).popBackStack();
                                }
                            }
                    )
        );
    }

    //load the comment details

    private void loadComments(){
        if (this.userId == null || this.deviceId == null) {
            NavDirections action = EventDetailsFragmentDirections.actionGlobalRegisterFragment();
            Navigation.findNavController(requireView()).navigate(action);
            return;
        }
        LoaderState loader = new ViewModelProvider(requireActivity()).get(LoaderState.class);
        loader.loadTask(
                commentModel.getAllComments(this.userId, this.deviceId, this.eventId)
                        .addOnSuccessListener(comments -> {
                            // use the adapter to display them

                            this.commentAdapter = new CommentAdapter(comments, this, this.eventId, isOrganizer, isAdmin);


                            binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
                            binding.commentsRecyclerView.setAdapter(commentAdapter);
                        })
                        .addOnFailureListener(ex -> {
                                    if (isAdded()) {
                                        ToastManager.show(requireContext(), "Failed to load comments", Toast.LENGTH_LONG);
                                        Navigation.findNavController(requireView()).popBackStack();
                                    }
                                }
                        )
        );

        binding.sendComment.setOnClickListener(v ->{

            String message = binding.typeComment.getText().toString();
            binding.typeComment.setText("");
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            String postTime = now.format(formatter);



            model.getUser(this.userId, this.deviceId).addOnSuccessListener(user -> {
                 String name = user.getName();

                commentModel.createComment(this.userId, this.deviceId, this.eventId, message, postTime).addOnSuccessListener(commentId -> {
                    Comment comment = new Comment(commentId, this.userId, message, postTime, name);
                    this.commentAdapter.addComment(comment);
                })

                .addOnFailureListener(exception -> {
                    Log.e("EventDetailsFragment", "Failed to delete an comment.", exception);

                    Toast.makeText(this.requireContext(), "Failed to add comment: " + exception.getMessage(), Toast.LENGTH_LONG).show();

                    if (exception instanceof FirebaseFunctionsException) {
                        Log.e("EventDetailsFragment", "FirebaseFunctionsException getCode() result: " + ((FirebaseFunctionsException) exception).getCode());
                    }
                });
            });




        });
    }


    // bind the event details to the
    // ui elemtents in the view
    private void bindEvent(Event event) {
        if (event == null) {
            return;
        }

        geolocationRequired = event.isGeolocationEnabled();
        isDrawn = event.isDrawn();

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
            this.imageModel.getImage(imageUuid, this.userId, this.deviceId)
                    .addOnSuccessListener(imageData -> {
                        Object imageBase64 = imageData.getImageData();
                        if (imageBase64 != null) {
                            Bitmap bitmap = decodeBase64ToBitmap(imageBase64.toString());
                            if (bitmap != null) {
                                this.binding.imagePreview.setImageBitmap(bitmap);
                            }
                        }
                    });
        }
    }



    // change text on the button to display according to role
    private void updateManageButton(String organizerId) {
        if (this.isAdmin && this.fromAdmin) {
            this.binding.enrollButton.setText("Delete");
            this.binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
            this.isOrganizer = false;
            return;
        }

        if (this.userId != null && organizerId != null && organizerId.equals(this.userId.toString())) {
            this.isOrganizer = true;
            this.binding.enrollButton.setText("Manage");
            this.binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
            this.binding.enrollButton.setOnClickListener(v -> {
                NavDirections action = EventDetailsFragmentDirections.actionEventdetailsfragmentToEventmanagerfragment(eventId, isDrawn);
                Navigation.findNavController(v).navigate(action);
            });
            return;
        }
        isOrganizer = false;
        binding.enrollButton.setOnClickListener(v -> toggleWaitlist());
    }

    // get the organizer name to display in the ui
    private void fetchOrganizerName(Event event) {
        if (this.userId == null || this.deviceId == null || this.eventId == null) {
            binding.textOrganizer.setText(" [unable to fetch organizer name]");
            return;
        }

        binding.textOrganizer.setText(" Loading organizer name...");

        eventModel.getOrganizerName(this.userId, this.deviceId, this.eventId)
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

                    this.binding.textOrganizer.setText(" Organized by " + name.trim());
                })
                .addOnFailureListener(exception -> {
                    ToastManager.show(requireContext(), "Failed to fetch organizer name", Toast.LENGTH_LONG);
                    Log.e("EventDetailsFragment", "Failed to fetch organizer name.", exception);

                    // set the organizer id as the name instead,
                    // to have something there at least, and to possibly help w/ debugging
                    this.binding.textOrganizer.setText(" Organized by " + event.getOrganizerId());
                });
    }

    private void updateWaitlistState() {
        if (this.isOrganizer || (this.isAdmin && this.fromAdmin)) {
            return;
        }
        if (this.userId == null || this.deviceId == null || this.eventId == null) {
            Log.d("EventDetails", "updateWaitlistState: missing session or eventId");
            return;
        }
        Log.d("EventDetails", "updateWaitlistState: checking waitlist for eventId=" + this.eventId);
        this.eventModel.checkWaitlist(this.userId, this.deviceId, this.eventId)
                .addOnSuccessListener(result -> {
                    inWaitlist = Boolean.TRUE.equals(result);
                    Log.d("EventDetails", "inWaitlist result=" + inWaitlist);
                    updateEnrollButtonLabel();
                });
    }

    private void refreshWaitlistCount() {
        if (this.userId == null || this.deviceId == null || this.eventId == null) {
            Log.d("EventDetails", "refreshWaitlistCount: missing session or eventId");
            return;
        }
        this.eventModel.getWaitlistCount(this.userId, this.deviceId, this.eventId)
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

        this.binding.textWaitingList.setText(" Wait list: " + waitlistCount);
    }

    private void updateEnrollButtonLabel() {
        if (!isAdded()) {
            return;
        }

        if (this.isOrganizer || (this.isAdmin && this.fromAdmin)) {
            return;
        }

        if (inWaitlist) {
            this.binding.enrollButton.setText("Leave Waitlist");
            this.binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_red));
        } else {
            this.binding.enrollButton.setText("Join Waitlist");
            this.binding.enrollButton.setBackgroundColor(getResources().getColor(R.color.color_light_green));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        this.binding = null;
    }

    private void toggleWaitlist() {
        if (this.userId == null || this.deviceId == null || this.eventId == null) {
            Log.d("EventDetails", "toggleWaitlist: missing session or eventId");
            return;
        }
        if (inWaitlist) {
            Log.d("EventDetails", "leaveWaitlist: eventId=" + this.eventId);
            this.eventModel.leaveWaitlist(this.userId, this.deviceId, this.eventId)
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
                    requireContext(),
                    "Location permission is required for this event",
                    Toast.LENGTH_LONG
            );
            return;
        }

        fusedLocationClient

                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location == null) {
                        ToastManager.show(
                                requireContext(),
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
                    Log.e("EventDetails", "getCurrentLocation failed", ex);
                    ToastManager.show(
                            requireContext(),
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
        eventModel.joinWaitlist(this.userId, this.deviceId, this.eventId, latitude, longitude, accuracyM)
                .addOnSuccessListener(_done -> {
                    inWaitlist = true;
                    Log.d("EventDetails", "joinWaitlist: success");
                    updateEnrollButtonLabel();
                    refreshWaitlistCount();
                    ToastManager.show(requireContext(), "Joined waitlist", Toast.LENGTH_LONG);
                })
                .addOnFailureListener(ex -> {
                    Log.e("EventDetails", "joinWaitlist: failed", ex);
                    ToastManager.show(requireContext(), "Failed to join waitlist", Toast.LENGTH_LONG);
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
