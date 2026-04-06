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
        infoStore.setSubtitle("Apply filters to search events.");
        infoStore.setIconRes(R.drawable.material_symbols_filter_alt_outline);

        // populate the filters with the current values

        Bundle filters = Navigation.findNavController(this.requireView())
                .getPreviousBackStackEntry()
                .getSavedStateHandle()
                .get("filters");

        if (filters != null) {
            this.populateFilterFields(filters);
        }

        this.binding.applyButton.setOnClickListener(v -> {
            Bundle result = new Bundle();

            String from = getTagValue(binding.inputFrom);
            String to = getTagValue(binding.inputTo);
            String search = getTextValue(binding.inputCategory);
            Integer capacity = getIntegerValue(binding.inputCapacity);

            if (from != null) result.putString("from", from);
            if (to != null) result.putString("to", to);
            if (search != null) result.putString("search", search);
            if (capacity != null) result.putInt("capacity", capacity);

            Navigation.findNavController(v)
                    .getPreviousBackStackEntry()
                    .getSavedStateHandle()
                    .set("filters", result);

            Navigation.findNavController(v).popBackStack();
        });

        this.binding.inputTo.setOnClickListener(v -> showDateTimePicker(binding.inputTo));
        this.binding.inputFrom.setOnClickListener(v -> showDateTimePicker(binding.inputFrom));

        this.binding.resetButton.setOnClickListener(v -> {
            this.binding.inputFrom.setText(null);
            this.binding.inputFrom.setTag(null);
            this.binding.inputTo.setText(null);
            this.binding.inputTo.setTag(null);
            this.binding.inputCategory.setText(null);
            this.binding.inputCategory.setTag(null);
        });
    }

    private void populateFilterFields(Bundle filters) {
        String from = filters.containsKey("from") ? filters.getString("from") : null;
        String to = filters.containsKey("to") ? filters.getString("to") : null;
        String search = filters.containsKey("search") ? filters.getString("search") : null;
        Integer capacity = filters.containsKey("capacity") ? filters.getInt("capacity") : null;

        if (from != null) this.binding.inputFrom.setText(from);
        if (to != null) this.binding.inputTo.setText(to);
        if (search != null) this.binding.inputCategory.setText(search);
        if (capacity != null) this.binding.inputCapacity.setText(String.valueOf(capacity));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
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
