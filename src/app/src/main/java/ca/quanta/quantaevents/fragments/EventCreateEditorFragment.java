package ca.quanta.quantaevents.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventCreateEditorBinding;
import ca.quanta.quantaevents.models.Event;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.utils.ToastManager;
import ca.quanta.quantaevents.viewmodels.EventViewModel;
import ca.quanta.quantaevents.viewmodels.ImageViewModel;

public class EventCreateEditorFragment extends Fragment {
    private static final String TAG = "EventCreateEditor";
    private FragmentEventCreateEditorBinding binding;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private final DateTimeFormatter utcFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private String selectedImageBase64;
    private EventViewModel eventModel;
    private ImageViewModel imageModel;
    private UUID userId;
    private UUID deviceId;
    private UUID eventId;
    private UUID existingImageId;
    private boolean imageDirty = false;
    private boolean imageRemoved = false;

    public EventCreateEditorFragment() {
        // Required empty public constructor
    }

    private SessionStore sessionStore;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventModel = new ViewModelProvider(this).get(EventViewModel.class);
        imageModel = new ViewModelProvider(this).get(ImageViewModel.class);
        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);

        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
            maybeLoadEventForEdit();
        });
        readEventId();

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Editor");
        infoStore.setSubtitle("Create or edit an event.");
        infoStore.setIconRes(R.drawable.material_symbols_add);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).popBackStack()
        );

        binding.saveButton.setOnClickListener(_view -> saveEvent());
        binding.inputRegistrationEnd.setOnClickListener(v -> showDateTimePicker(binding.inputRegistrationEnd));
        binding.inputStartTime.setOnClickListener(v -> showDateTimePicker(binding.inputStartTime));
        binding.inputRegistrationStart.setOnClickListener(v -> showDateTimePicker(binding.inputRegistrationStart));

        binding.btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        binding.btnRemoveImage.setOnClickListener(v -> clearSelectedImage());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventCreateEditorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToastManager.cancel();
        binding = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    selectedImageUri = uri;
                    imageDirty = true;
                    imageRemoved = false;
                    try {
                        selectedImageBase64 = encodeImageToBase64(uri);
                        showPreviewFromBase64(selectedImageBase64);
                    } catch (IOException ex) {
                        selectedImageBase64 = null;
                        ToastManager.show(getContext(), "Failed to read image", Toast.LENGTH_LONG);
                    }
                });
    }

    private void saveEvent() {
        String name = safeText(binding.inputName.getText());
        String description = safeText(binding.inputDescription.getText());
        String registrationStart = getUtcValue(binding.inputRegistrationStart);
        String registrationEnd = getUtcValue(binding.inputRegistrationEnd);
        String eventTime = getUtcValue(binding.inputStartTime);
        String location = safeText(binding.inputLocation.getText());
        Integer eventCapacity = parseInt(binding.inputEventLimit.getText());
        Integer registrationLimit = parseInt(binding.inputRegistrationCapacity.getText());
        boolean geolocation = binding.checkGeolocation.isChecked();
        String eventCategory = normalizeEmpty(safeText(binding.inputCategory.getText()));
        String eventGuidelines = normalizeEmpty(safeText(binding.inputGuidelines.getText()));
        boolean isPrivate = binding.checkPrivate.isChecked();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(registrationStart) || TextUtils.isEmpty(registrationEnd)
                || TextUtils.isEmpty(eventTime) || TextUtils.isEmpty(location) || eventCapacity == null) {
            ToastManager.show(getContext(), "Fill required fields", Toast.LENGTH_LONG);
            return;
        }
        if (userId == null || deviceId == null) {
            ToastManager.show(getContext(), "Missing user session", Toast.LENGTH_LONG);
            return;
        }

        binding.saveButton.setEnabled(false);

        if (eventId != null) {
            updateEventFlow(registrationStart, registrationEnd, eventTime, name, description,
                    eventCategory, eventGuidelines, geolocation, eventCapacity, location, registrationLimit, isPrivate);
            return;
        }

        if (selectedImageBase64 != null) {
            imageModel.createImage(userId, deviceId, selectedImageBase64)
                    .addOnSuccessListener(imageId -> createEventWithImageId(imageId, registrationStart, registrationEnd,
                            eventTime, name, description, eventCategory, eventGuidelines, geolocation,
                            eventCapacity, location, registrationLimit, isPrivate))
                    .addOnFailureListener(ex -> {
                        binding.saveButton.setEnabled(true);
                        Log.e(TAG, "Failed to upload image", ex);
                        ToastManager.show(getContext(), "Failed to upload image", Toast.LENGTH_LONG);
                    });
        } else {
            createEventWithImageId(null, registrationStart, registrationEnd, eventTime, name, description,
                    eventCategory, eventGuidelines, geolocation, eventCapacity, location, registrationLimit, isPrivate);
        }
    }

    private void clearSelectedImage() {
        selectedImageUri = null;
        binding.imagePreview.setImageDrawable(null);
        binding.imagePreview.setVisibility(View.GONE);
        selectedImageBase64 = null;
        imageDirty = true;
        imageRemoved = true;
    }

    private void showDateTimePicker(TextInputEditText target) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), R.style.ThemeOverlay_QuantaEvents_DatePicker,
                (dateView, year, month, dayOfMonth) -> {
                    new TimePickerDialog(requireContext(), R.style.ThemeOverlay_QuantaEvents_TimePicker,
                            (timeView, hourOfDay, minute) -> {
                                LocalDateTime local = LocalDateTime.of(year, month + 1, dayOfMonth,
                                        hourOfDay, minute);
                                ZonedDateTime zonedLocal = local.atZone(ZoneId.systemDefault());
                                ZonedDateTime utc = zonedLocal.withZoneSameInstant(ZoneId.of("UTC"));
                                String display = zonedLocal.format(displayFormatter);
                                String utcValue = utc.format(utcFormatter);
                                target.setText(display);
                                target.setTag(utcValue);
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String getUtcValue(TextInputEditText input) {
        Object tag = input.getTag();
        if (tag instanceof String) {
            return (String) tag;
        }
        return safeText(input.getText());
    }

    private void readEventId() {
        ca.quanta.quantaevents.fragments.EventCreateEditorFragmentArgs args = ca.quanta.quantaevents.fragments.EventCreateEditorFragmentArgs.fromBundle(getArguments());
        eventId = args.getEventId();
        maybeLoadEventForEdit();
    }

    private void maybeLoadEventForEdit() {
        if (eventId == null || userId == null || deviceId == null) {
            return;
        }
        eventModel.getEvent(eventId, userId, deviceId)
                .addOnSuccessListener(this::bindEventForEdit)
                .addOnFailureListener(ex ->
                        ToastManager.show(getContext(), "Failed to load event", Toast.LENGTH_LONG)
                );
    }

    private void bindEventForEdit(Event event) {
        if (event == null) {
            return;
        }
        existingImageId = event.getImageId();
        imageDirty = false;
        imageRemoved = false;
        binding.inputName.setText(stringValue(event.getEventName(), ""));
        binding.inputDescription.setText(stringValue(event.getEventDescription(), ""));
        binding.inputLocation.setText(stringValue(event.getLocation(), ""));
        binding.inputCategory.setText(stringValue(event.getEventCategory(), ""));
        binding.inputGuidelines.setText(stringValue(event.getEventGuidelines(), ""));
        binding.checkGeolocation.setChecked(event.isGeolocationEnabled());

        if (event.getRegistrationStartTime() != null) {
            setDateTimeField(binding.inputRegistrationStart, event.getRegistrationStartTime());
        }
        if (event.getRegistrationEndTime() != null) {
            setDateTimeField(binding.inputRegistrationEnd, event.getRegistrationEndTime());
        }
        if (event.getEventTime() != null) {
            setDateTimeField(binding.inputStartTime, event.getEventTime());
        }

        if (event.getEventCapacity() != null && event.getEventCapacity() > 0) {
            binding.inputEventLimit.setText(event.getEventCapacity().toString());
        }
        if (event.getRegistrationLimit() != null) {
            binding.inputRegistrationCapacity.setText(event.getRegistrationLimit().toString());
        }

        UUID imageUuid = event.getImageId();
        if (imageUuid != null) {
            imageModel.getImage(imageUuid, userId, deviceId)
                    .addOnSuccessListener(imageData -> {
                        Object imageBase64 = imageData.getImageData();
                        if (imageBase64 != null) {
                            selectedImageBase64 = imageBase64.toString();
                            showPreviewFromBase64(selectedImageBase64);
                        }
                    });
        }
    }

    // The following function is from/based off OpenAI, ChatGPT, "encodeImagetoBase64 which encodes the image to a format(Base64) which can be stored on firebase", 2026-03-11
    private String encodeImageToBase64(Uri uri) throws IOException {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (inputStream == null) {
                throw new IOException("Unable to open image");
            }
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            byte[] bytes = outputStream.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        }
    }

    private void showPreviewFromBase64(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bitmap == null) {
            ToastManager.show(getContext(), "Unable to preview image", Toast.LENGTH_LONG);
            return;
        }
        binding.imagePreview.setImageBitmap(bitmap);
        binding.imagePreview.setVisibility(View.VISIBLE);
    }

    private void createEventWithImageId(@Nullable UUID imageId, String registrationStart,
                                        String registrationEnd, String eventTime,
                                        String name, String description,
                                        String eventCategory, String eventGuidelines,
                                        boolean geolocation, int eventCapacity,
                                        String location, Integer registrationLimit, boolean isPrivate) {
        UUID imageUuid = null;
        if (imageId != null) {
            try {
                imageUuid = imageId;
            } catch (IllegalArgumentException ignored) {
                imageUuid = null;
            }
        }

        eventModel.createEvent(userId, deviceId, registrationStart, registrationEnd, eventTime,
                        name, description, eventCategory, eventGuidelines, geolocation,
                        eventCapacity, location, registrationLimit, imageUuid, isPrivate)
                .addOnSuccessListener(eventId -> {
                    binding.saveButton.setEnabled(true);
                    ToastManager.show(getContext(), "Event created", Toast.LENGTH_LONG);
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                })
                .addOnFailureListener(ex -> {
                    binding.saveButton.setEnabled(true);
                    Log.e(TAG, "Failed to create event", ex);
                    ToastManager.show(getContext(), "Failed to create event", Toast.LENGTH_LONG);
                });
    }

    private void updateEventFlow(String registrationStart, String registrationEnd, String eventTime,
                                 String name, String description,
                                 String eventCategory, String eventGuidelines,
                                 boolean geolocation, int eventCapacity,
                                 String location, Integer registrationLimit, boolean isPrivate) {
        if (imageDirty) {
            if (imageRemoved) {
                updateEventWithImageId(null, registrationStart, registrationEnd, eventTime,
                        name, description, eventCategory, eventGuidelines, geolocation,
                        eventCapacity, location, registrationLimit);
            } else if (selectedImageBase64 != null) {
                imageModel.createImage(userId, deviceId, selectedImageBase64)
                        .addOnSuccessListener(imageId -> updateEventWithImageId(imageId, registrationStart, registrationEnd,
                                eventTime, name, description, eventCategory, eventGuidelines, geolocation,
                                eventCapacity, location, registrationLimit))
                        .addOnFailureListener(ex -> {
                            binding.saveButton.setEnabled(true);
                            Log.e(TAG, "Failed to upload image", ex);
                            ToastManager.show(getContext(), "Failed to upload image", Toast.LENGTH_LONG);
                        });
            } else {
                updateEventWithImageId(null, registrationStart, registrationEnd, eventTime,
                        name, description, eventCategory, eventGuidelines, geolocation,
                        eventCapacity, location, registrationLimit, isPrivate);
            }
            return;
        }
        updateEventWithImageId(existingImageId == null ? null : existingImageId,
                registrationStart, registrationEnd, eventTime, name, description,
                eventCategory, eventGuidelines, geolocation, eventCapacity, location, registrationLimit, isPrivate);
    }

    private void updateEventWithImageId(@Nullable UUID imageId, String registrationStart,
                                        String registrationEnd, String eventTime,
                                        String name, String description,
                                        String eventCategory, String eventGuidelines,
                                        boolean geolocation, int eventCapacity,
                                        String location, Integer registrationLimit, boolean isPrivate) {
        UUID imageUuid = null;
        if (imageId != null) {
            try {
                imageUuid = imageId;
            } catch (IllegalArgumentException ignored) {
                imageUuid = null;
            }
        }
        eventModel.updateEvent(userId, deviceId, eventId,
                        registrationStart, registrationEnd, eventTime,
                        name, description, eventCategory, eventGuidelines,
                        geolocation, eventCapacity, location, registrationLimit, imageUuid, isPrivate)
                .addOnSuccessListener(_done -> {
                    binding.saveButton.setEnabled(true);
                    ToastManager.show(getContext(), "Event updated", Toast.LENGTH_LONG);
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                })
                .addOnFailureListener(ex -> {
                    binding.saveButton.setEnabled(true);
                    Log.e(TAG, "Failed to update event", ex);
                    ToastManager.show(getContext(), "Failed to update event", Toast.LENGTH_LONG);
                });
    }


    private static String safeText(@Nullable CharSequence text) {
        return text == null ? "" : text.toString().trim();
    }

    private static String stringValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String result = value.toString().trim();
        return result.isEmpty() ? fallback : result;
    }

    @Nullable
    private static String normalizeEmpty(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }

    private void setDateTimeField(TextInputEditText input, ZonedDateTime value) {
        try {
            ZonedDateTime local = value.withZoneSameInstant(ZoneId.systemDefault());
            input.setText(local.format(displayFormatter));
            input.setTag(value.format(utcFormatter));
        } catch (Exception ignored) {
            input.setText(value.toString());
        }
    }

    private static Integer parseInt(@Nullable CharSequence text) {
        if (text == null) {
            return null;
        }
        String value = text.toString().trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
