package ca.quanta.quantaevents.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import ca.quanta.quantaevents.databinding.FragmentEventFilterBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;

public class EventFilterFragment extends Fragment {
    private FragmentEventFilterBinding binding;
    private final DateTimeFormatter displayFormatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
    private final DateTimeFormatter utcFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // set title of the page to Filter Events and the subtitle.
        // also sets the icon for the page
        infoStore.setTitle("Filter Events");
        infoStore.setSubtitle("Apply filters to search events");
        infoStore.setIconRes(R.drawable.material_symbols_filter_alt_outline);

        binding.applyButton.setOnClickListener(v -> {
            Bundle result = new Bundle();
            String from = getTagValue(binding.inputFrom);
            String to = getTagValue(binding.inputTo);
            String category = getTextValue(binding.inputCategory);
            Integer capacity = getIntegerValue(binding.inputCapacity);

            if (from != null) result.putString("from", from);
            if (to != null) result.putString("to", to);
            if (category != null) result.putString("category", category);
            if (capacity != null) result.putInt("capacity", capacity);

            Navigation.findNavController(v)
                    .getPreviousBackStackEntry()
                    .getSavedStateHandle()
                    .set("filters", result);

            Navigation.findNavController(v).popBackStack();
        });

        binding.inputTo.setOnClickListener(v -> showDateTimePicker(binding.inputTo));
        binding.inputFrom.setOnClickListener(v -> showDateTimePicker(binding.inputFrom));

        binding.resetButton.setOnClickListener(v -> {
            binding.inputFrom.setText(null);
            binding.inputFrom.setTag(null);
            binding.inputTo.setText(null);
            binding.inputTo.setTag(null);
            binding.inputCategory.setText(null);
            binding.inputCategory.setTag(null);
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentEventFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
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


    /**
     * Gets the value stored in the tag of the field, which is used to store the UTC value of the date time.
     * @param field The text input field.
     * @return Value stored in the text input's tag.
     */
    @Nullable
    private String getTagValue(TextInputEditText field) {
        Object tag = field.getTag();
        return tag != null ? tag.toString() : null;
    }

    /**
     * Gets the text value of the field, which is used for the category filter.
     * @param field The text input field.
     * @return Text entered in field if text is entered, null otherwise.
     */
    @Nullable
    private String getTextValue(TextInputEditText field) {
        CharSequence text = field.getText();
        if (text == null || text.toString().trim().isEmpty()) return null;
        return text.toString().trim();
    }

    /**
     * Gets the number entered in the field, used for capacity filter.
     * @param field The number input field.
     * @return Number entered in field if a valid number is entered, null otherwise.
     */
    private Integer getIntegerValue(TextInputEditText field) {
        CharSequence text = field.getText();
        if (text == null || text.toString().trim().isEmpty()) {
            return null;
        }
        else {
            String numberText = text.toString().trim();
            try {
                Integer capacityText = Integer.parseInt(numberText);
                return capacityText;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
