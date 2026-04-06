package ca.quanta.quantaevents.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import ca.quanta.quantaevents.R;
import ca.quanta.quantaevents.databinding.FragmentAccountBinding;
import ca.quanta.quantaevents.databinding.FragmentAdminAccountEditBinding;
import ca.quanta.quantaevents.models.User;
import ca.quanta.quantaevents.stores.FragmentInfoStore;
import ca.quanta.quantaevents.stores.SessionStore;
import ca.quanta.quantaevents.viewmodels.UserViewModel;

public class AdminAccountEditFragment extends Fragment {

    private UUID userId;
    private UUID deviceId;
    private SessionStore sessionStore;

    private FragmentAdminAccountEditBinding binding;
    private UserViewModel userModel;

    public AdminAccountEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentInfoStore infoStore = new ViewModelProvider(requireActivity()).get(FragmentInfoStore.class);
        // sets title as account
        // sets subtitle and the icon
        infoStore.setTitle("Account");
        infoStore.setSubtitle("Change account details");
        infoStore.setIconRes(R.drawable.material_symbols_person_outline);

        sessionStore = new ViewModelProvider(requireActivity()).get(SessionStore.class);
        // verify and check session
        sessionStore.observeSession(getViewLifecycleOwner(), (uid, did) -> {
            userId = uid;
            deviceId = did;
        });

        userModel = new ViewModelProvider(this).get(UserViewModel.class);

        AdminAccountEditFragmentArgs args = AdminAccountEditFragmentArgs.fromBundle(this.getArguments());

        binding.saveButton.setOnClickListener(
                v -> {

                    Boolean becomeEntrant = binding.checkEntrant.isChecked();
                    Boolean becomeOrganizer = binding.checkOrganizer.isChecked();
                    Boolean becomeAdmin = binding.checkAdmin.isChecked();

                    userModel.updateRoles(this.userId, this.deviceId, args.getUserId(), becomeEntrant, becomeOrganizer, becomeAdmin);

                    Navigation.findNavController(v).popBackStack();
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAdminAccountEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}