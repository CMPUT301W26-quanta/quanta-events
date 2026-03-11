package ca.quanta.quantaevents.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.google.android.material.textfield.TextInputEditText;
import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentAdminProfileBrowserBinding;
import ca.quanta.quantaevents.viewmodels.UserViewModel;
import ca.quanta.quantaevents.databinding.FragmentRegisterBinding;
import ca.quanta.quantaevents.stores.FragmentInfoStore;


/**
 * Class which defines the registration screen.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {
    private FragmentRegisterBinding binding;

    private TextInputEditText name;
    private TextInputEditText email;
    private TextInputEditText phone;
    private CheckBox isEntrant;
    private CheckBox isOrganizer;
    private CheckBox isAdmin;
    private CheckBox getNotifications;
    private UserViewModel model;

    /**
     * Constructor for a RegisterFragment object.
     */
    public RegisterFragment() {
        super();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        infoStore.setTitle("Welcome!");
        infoStore.setSubtitle("Get started by creating an account.");
        infoStore.setIconRes(R.drawable.material_symbols_waving_hand_outline);

        Button saveButton = view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(this);

        name = view.findViewById(R.id.input_name);
        email = view.findViewById(R.id.input_email);
        phone = view.findViewById(R.id.input_phone);
        isEntrant = view.findViewById(R.id.check_entrant);
        isOrganizer = view.findViewById(R.id.check_organizer);
        isAdmin = view.findViewById(R.id.check_admin);
        getNotifications = view.findViewById(R.id.check_notifications);

        // **** set up buttons

        binding.saveButton.setOnClickListener(
                v -> Navigation.findNavController(v).navigate(R.id.action_registerfragment_to_homefragment)
        );
    }

    /**
     * Listener for the save button that extracts text input and check box values.
     * @param v The fragment which the button is associated with.
     */
    @Override
    public void onClick(View v) {
        String name = this.name.getText().toString();
        String email = this.email.getText().toString();
        String phone = this.phone.getText().toString();
        Boolean isEntrant = this.isEntrant.isChecked();
        Boolean isOrganizer = this.isOrganizer.isChecked();
        Boolean isAdmin = this.isAdmin.isChecked();
        Boolean getNotifications = this.getNotifications.isChecked();
        UUID deviceId = UUID.randomUUID();
        model.createUser(name, email, phone, isEntrant, isOrganizer, isAdmin, getNotifications, deviceId);
    }
}
