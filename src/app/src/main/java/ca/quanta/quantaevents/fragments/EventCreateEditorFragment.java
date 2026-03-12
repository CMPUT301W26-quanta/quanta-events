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

        binding.saveButton.setOnClickListener(_view -> createEvent());
        binding.inputRegistrationEnd.setOnClickListener(v -> showDateTimePicker(binding.inputRegistrationEnd));
        binding.inputStartTime.setOnClickListener(v -> showDateTimePicker(binding.inputStartTime));
        binding.inputEndTime.setOnClickListener(v -> showDateTimePicker(binding.inputEndTime));

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    selectedImageUri = uri;
                    try {
                        selectedImageBase64 = encodeImageToBase64(uri);
                        showPreviewFromBase64(selectedImageBase64);
                    } catch (IOException ex) {
                        selectedImageBase64 = null;
                        Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createEvent() {
        // TODO: Handle event updates
        String name = safeText(binding.inputName.getText());
        String description = safeText(binding.inputDescription.getText());
        String start = getUtcValue(binding.inputStartTime);
        String end = getUtcValue(binding.inputRegistrationEnd);
        String location = safeText(binding.inputLocation.getText());
        Integer limit = parseInt(binding.inputRegistrationLimit.getText());

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(start) || TextUtils.isEmpty(end) || TextUtils.isEmpty(location)) {
            Toast.makeText(requireContext(), "Fill required fields", Toast.LENGTH_LONG).show();
            return;
        }
        if (userId == null || deviceId == null) {
            Toast.makeText(requireContext(), "Missing user session", Toast.LENGTH_LONG).show();
            return;
        }

        binding.saveButton.setEnabled(false);

        if (selectedImageBase64 != null) {
            imageModel.createImage(userId, deviceId, selectedImageBase64)
                    .addOnSuccessListener(imageId -> createEventWithImageId(imageId, start, end, name,
                            description, location, limit))
                    .addOnFailureListener(ex -> {
                        binding.saveButton.setEnabled(true);
                        Log.e(TAG, "Failed to upload image", ex);
                        Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_LONG).show();
                    });
        } else {
            createEventWithImageId(null, start, end, name, description, location, limit);
        }
    }

    private void clearSelectedImage() {
        selectedImageUri = null;
        binding.imagePreview.setImageDrawable(null);
        binding.imagePreview.setVisibility(View.GONE);
        selectedImageBase64 = null;
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
        EventCreateEditorFragmentArgs args = EventCreateEditorFragmentArgs.fromBundle(getArguments());
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
                        Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_LONG).show()
                );
    }

    private void bindEventForEdit(Event event) {
        if (event == null) {
            return;
        }
        binding.inputName.setText(stringValue(event.getEventName(), ""));
        binding.inputDescription.setText(stringValue(event.getEventDescription(), ""));
        binding.inputLocation.setText(stringValue(event.getLocation(), ""));

        if (event.getRegistrationStartTime() != null) {
            setDateTimeField(binding.inputStartTime, event.getRegistrationStartTime());
        }
        if (event.getRegistrationEndTime() != null) {
            setDateTimeField(binding.inputRegistrationEnd, event.getRegistrationEndTime());
        }

        if (event.getRegistrationLimit() != null) {
            binding.inputRegistrationLimit.setText(event.getRegistrationLimit().toString());
        }

        UUID imageUuid = event.getImageId();
        if (imageUuid != null) {
            imageModel.getImage(imageUuid, userId, deviceId)
                    .addOnSuccessListener(imageData -> {
                        Object imageBase64 = imageData.get("imageData");
                        if (imageBase64 != null) {
                            selectedImageBase64 = imageBase64.toString();
                            showPreviewFromBase64(selectedImageBase64);
                        }
                    });
        }
    }


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
            Toast.makeText(requireContext(), "Unable to preview image", Toast.LENGTH_LONG).show();
            return;
        }
        binding.imagePreview.setImageBitmap(bitmap);
        binding.imagePreview.setVisibility(View.VISIBLE);
    }

    private void createEventWithImageId(@Nullable String imageId, String start, String end,
                                        String name, String description, String location,
                                        Integer limit) {
        UUID imageUuid = null;
        if (imageId != null) {
            try {
                imageUuid = UUID.fromString(imageId);
            } catch (IllegalArgumentException ignored) {
                imageUuid = null;
            }
        }

        eventModel.createEvent(userId, deviceId, start, end, name, description, location, limit, imageUuid)
                .addOnSuccessListener(eventId -> {
                    binding.saveButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Event created", Toast.LENGTH_LONG).show();
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                })
                .addOnFailureListener(ex -> {
                    binding.saveButton.setEnabled(true);
                    Log.e(TAG, "Failed to create event", ex);
                    Toast.makeText(requireContext(), "Failed to create event", Toast.LENGTH_LONG).show();
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

    private void setDateTimeField(TextInputEditText input, ZonedDateTime value) {
        try {
            ZonedDateTime local = value.withZoneSameInstant(ZoneId.systemDefault());
            input.setText(local.format(displayFormatter));
            input.setTag(value.format(utcFormatter));
        } catch (Exception ignored) {
            input.setText(value.toString());
        }
    }

    private static UUID parseUUID(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
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
