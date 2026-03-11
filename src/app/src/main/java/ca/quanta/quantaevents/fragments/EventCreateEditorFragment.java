package ca.quanta.quantaevents.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentEventCreateEditorBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventCreateEditorFragment extends Fragment {
    private FragmentEventCreateEditorBinding binding;
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedImageUri;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private final DateTimeFormatter utcFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public EventCreateEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Event Editor");
        infoStore.setSubtitle("Create or edit an event.");
        infoStore.setIconRes(R.drawable.material_symbols_add);

        binding.backButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_eventeditorfragment_to_eventdashboardfragment)
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
                    binding.imagePreview.setImageURI(uri);
                    binding.imagePreview.setVisibility(View.VISIBLE);
                });
    }

    private void createEvent() {
        String name = safeText(binding.inputName.getText());
        String description = safeText(binding.inputDescription.getText());
        String start = getUtcValue(binding.inputStartTime);
        String end = getUtcValue(binding.inputRegistrationEnd);
        Integer limit = parseInt(binding.inputRegistrationLimit.getText());

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(description)
                || TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            Toast.makeText(requireContext(), "Fill required fields", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void clearSelectedImage() {
        selectedImageUri = null;
        binding.imagePreview.setImageDrawable(null);
        binding.imagePreview.setVisibility(View.GONE);
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

    private static String safeText(@Nullable CharSequence text) {
        return text == null ? "" : text.toString().trim();
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
